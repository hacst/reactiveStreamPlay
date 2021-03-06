package backend.flowNetwork.transformations

import akka.actor.Props
import backend.NextFlowUID
import backend.flowNetwork.{FlowNode, FlowFieldOfInterest, TargetableFlow}
import backend.flowTypes.{FlowObject, WordObject}

object FlowTokenizer {
  var nodeType = "Tokenizer"
  def props(id:Long, name: String,  x: Int, y: Int): Props = Props(new FlowTokenizer(id, name, x, y))
}

/**
 * Splits the FOI field of incoming messages into Word messages to the target.
 *
 * @param id Unique numeric ID of this actor
 * @param name Display name for this actor
 * @param x X coordinate on screen
 * @param y Y coordinate on screen
 */
class FlowTokenizer(id: Long, name: String,  x: Int, y: Int)
  extends FlowNode(id, name, FlowTokenizer.nodeType, x, y, 1 ,1) with TargetableFlow with FlowFieldOfInterest {

  var separators = Array[Char](' ','.',',','!','?','\n')

  val configStringSeperator = " and "

  addConfigMapGetters(() => Map(
    "separators" -> separators.mkString,
    "display" -> "separators,field"
  ))

  addConfigSetters({
    case ("separators", sep) =>
      log.info(s"Updating separators to chars: $sep")
      separators = sep.toCharArray
  })

  override def active: Receive = {
    case o: FlowObject =>
      o.contentAsString(fieldOfInterest) match {
        case Some(content) =>
          content.split(separators)
                 .filter(!_.isEmpty)
                 .foreach(target ! WordObject(NextFlowUID(), o, _))

        case None => log.debug(s"Message ${o.uid} doesn't have a String convertible field $fieldOfInterest")
      }
  }
}
