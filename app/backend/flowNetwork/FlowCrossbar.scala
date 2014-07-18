package backend.flowNetwork

import akka.actor.{ActorRef, Actor, Props}
import akka.event.Logging
import backend.flowTypes.FlowObject

case class MembershipUpdate(targets: Set[ActorRef])



object FlowCrossbar {
  def props(id:Long, name: String,  x: Int, y: Int): Props = Props(new FlowCrossbar(id, name, x, y))
}

class FlowCrossbar(id: Long, name: String,  x: Int, y: Int)
  extends FlowNode(id, name, x, y, 3 , 3) {

  var targets = Set[ActorRef]()

  override def receive = super.receive orElse {
    case o: FlowObject =>
      log.debug(s"Repeating $o to ${targets.size} targets")
      targets.map(_ ! o)

    case AddTarget(t) =>
      log.info(s"New target $t on $this")
      context.system.eventStream.publish(MembershipUpdate(targets)) //TODO: Replace this
      targets = targets + t

    case RemoveTarget(t) =>
      if (targets contains t) {
        log.info(s"Removed target $t on $this")
        targets = targets - t
        context.system.eventStream.publish(MembershipUpdate(targets)) //TODO: Replace this
      }
  }
}
