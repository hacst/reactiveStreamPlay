package backend.flowNetwork

import akka.actor.{Props, ActorRef, Actor}
import akka.event.Logging
import scala.util.Random
import backend.flowTypes.Sentiment
import scala.concurrent.duration._

object FlowSource {
  def props(): Props = Props(new FlowSource)
}

class FlowSource extends TargetableFlow {
  import context.dispatcher

  private case object Tick

  val tick = context.system.scheduler.schedule(1 second, 1 second, self, Tick)
  var uid: Long = 1

  override def postStop() = tick.cancel()

  override def passive = {
    case Tick => // Nothing
  }
  override def active = {
    case Tick =>
      target ! Sentiment(uid, 0, Random.nextDouble)
      uid += 1
  }
}
