package backend.flowNetwork

import akka.actor.{Props, ActorRef, Actor}
import backend.NextFlowUID
import backend.flowTypes.TwitterMessage
import scala.concurrent.duration._
import external.LoremIpsum

object FlowIpsumSource {
  var nodeType = "FlowIpsumSource"
  def props(id:Long, name: String,  x: Int, y: Int): Props = Props(new FlowIpsumSource(id, name, x, y))
}

class FlowIpsumSource(id: Long, name: String,  x: Int, y: Int)
  extends FlowNode(id, name, FlowIpsumSource.nodeType, x, y, 1, 0) with TargetableFlow {

  import context.dispatcher

  private case object Tick

  val tick = context.system.scheduler.schedule(1 second, 1 second, self, Tick)
  var count: Long = 0

  addConfigMapGetters(() => Map(
    "sourced" -> count.toString,
    "display" -> "sourced"
  ))

  override def postStop() = tick.cancel()

  override def passive = {
    case Tick => // Nothing
  }
  override def active = {
    case Tick =>
      val sentence =
      target ! TwitterMessage(NextFlowUID(), LoremIpsum.sentence)
      count += 1
      configUpdated()
  }
}