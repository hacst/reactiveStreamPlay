package backend.flowNetwork

import akka.actor._
import play.api.libs.json.JsValue
import play.api.libs.iteratee.Concurrent.Channel
import backend.NextFlowUID

case class Register(observer: ActorRef)

case object DetectConfiguration

case class LookupObj(id: Long)
case class LookupId(obj: ActorRef)

case object GetFlowObjectTypes
case class CreateFlowObject(what: String, x: Int, y: Int)
case class DeleteFlowObject(id: Long)

case object GetConnections
case class Connect(source: Long, target: Long)
case class Disconnect(source: Long, target: Long)

case class EventChannel(chan: Channel[JsValue])

object FlowSupervisor {
  def props(): Props = Props(new FlowSupervisor)
}

class FlowSupervisor extends Actor with ActorLogging {
  import akka.actor.OneForOneStrategy
  import akka.actor.SupervisorStrategy._
  import scala.concurrent.duration._

  val ordinaryFlowObjects = Map[String, (Long, String, Int, Int) => Props](
    FlowSource.nodeType -> FlowSource.props,
    FlowCrossbar.nodeType -> FlowCrossbar.props,
    FlowFilter.nodeType -> FlowFilter.props,
    FlowTokenizer.nodeType -> FlowTokenizer.props,
    FlowFrequency.nodeType -> FlowFrequency.props,
    FlowSentiment.nodeType -> FlowSentiment.props,
    FlowAccumulator.nodeType -> FlowAccumulator.props
  )

  object newActorName {
    var objectCounts = scala.collection.mutable.Map[String, Int]().withDefaultValue(0)
    def apply(name: String) = {
      objectCounts(name) += 1
      s"$name${objectCounts(name)}"
    }
  }

  var flowIdToObject = scala.collection.mutable.Map.empty[Long, ActorRef]
  var flowObjectToId = scala.collection.mutable.Map.empty[ActorRef, Long]
  var connections = scala.collection.mutable.Map.empty[(ActorRef, ActorRef), ActorRef]
  var observers = scala.collection.mutable.Set.empty[ActorRef]

  private def connectionsFor(obj: ActorRef): scala.collection.mutable.Map[(ActorRef, ActorRef), ActorRef] =
    connections.filter { case ((source, target),_) => (source == obj || target == obj) }

  def receive = {
    case GetFlowObjectTypes => sender() ! ordinaryFlowObjects.keys.toList

    case EventChannel(channel: Channel[JsValue]) =>
      log.info("Starting event channel translator")
      val translator = context.actorOf(MessageTranslator.props(channel), name = "messageTranslator")
      self ! Register(translator)

    case Register(observer) =>
      log.info(s"${observer} registered for updates")
      observers += observer

    case DetectConfiguration =>
      log.info(s"${sender()} triggered configuration detection")
      // Request all node configurations
      flowObjectToId foreach { case (obj, _) => obj ! GetConfiguration }
      //TODO: Should be able to provide connection information too

    case CreateFlowObject(objectType, x, y) =>
      ordinaryFlowObjects.get(objectType) match {
        case Some(_) => {
          log.info(s"Creating new $objectType for ${
            sender()
          }")
          val name = newActorName(objectType)
          val id = NextFlowUID()
          val obj = context.actorOf(ordinaryFlowObjects(objectType)(id, name, x, y), name = name)
          flowIdToObject += id -> obj
          flowObjectToId += obj -> id

          obj ! GetConfiguration // Pull configuration
          sender() ! (id, obj)
        }
        case None =>
          log.warning(s"Asked to create unknown flow object type $objectType")
      }

    case DeleteFlowObject(id: Long) =>
      flowIdToObject.get(id) match {
        case Some(obj) => {
          log.info(s"Deleting node $id")

          // First disconnect
          connectionsFor(obj) map { case ((source, target), _) => disconnect (source, target) }
          // Then delete
          obj ! Kill
          flowIdToObject.remove(id)
          flowObjectToId.remove(obj)

          sender() ! id // Ack delete of ID
          observers map { (o) => o ! (id, None) } // Notify observers
        }
        case None =>
          log.warning(s"Asked to delete unknown object $id")
      }

    case Configuration(data) =>
      flowObjectToId.get(sender()) match {
        case Some(id) => observers map { (o) => o ! (id, Configuration(data)) }
        case None => log.error(s"Received configuration update from untracked actor ${sender()}")
      }

    case (id: Long, Configuration(data)) =>
      // Forward config to addressed actor
      flowIdToObject.get(id) match {
        case Some(obj) => obj ! Configuration(data)
        case None => log.error(s"Asked to forward configuration for unknown id $id")
      }

    case LookupObj(id) =>
      flowIdToObject.get(id) match {
        case Some(obj) => sender() ! Some(obj)
        case None => sender() ! None
      }

    case LookupId(obj) =>
      flowObjectToId.get(obj) match {
        case Some(id) => sender() ! Some(id)
        case None => sender() ! None
      }

    case GetConnections =>
      sender() ! connections

    case Connect(sourceId, targetId) =>
      (flowIdToObject.get(sourceId), flowIdToObject.get(targetId)) match {
        case (Some(source), Some(target)) if !connections.contains {(source, target)} => {
          log.info(s"Creating new connection from $source to $target")
          val connection = context.actorOf(FlowConnection.props(source, target), name = newActorName("FlowConnection"))
          connections += (source, target) -> connection
          source ! AddTarget(connection)
          sender() ! ((sourceId, targetId), connection)
        }
        case _ => log.warning(s"Asked to connect $sourceId with $targetId of which are invalid or already connected")
      }

    case Disconnect(sourceId, targetId) =>
      (flowIdToObject.get(sourceId), flowIdToObject.get(targetId)) match {
        case (Some(source), Some(target)) => {
          disconnect(source, target)
          sender() ! (sourceId, targetId) // Ack disconnect
        }
        case _ =>
          log.warning(s"Asked to connect $sourceId with $targetId of which at least one is unknown")
          sender() ! (sourceId, targetId) // Those certainly aren't connected
      }
  }

  private def disconnect(source: ActorRef, target: ActorRef) =
    connections.remove((source, target)) match {
      case Some(connection) =>
        log.info(s"Disconnecting $source and $target")
        source ! RemoveTarget(connection)
        connection ! Kill
        true

      case _ =>
        log.warning(s"Asked to disconnect $source from $target but have no connection")
        false
    }
}
