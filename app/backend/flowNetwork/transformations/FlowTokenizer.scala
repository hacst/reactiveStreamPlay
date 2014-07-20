package backend.flowNetwork.transformations

import akka.actor.Props
import backend.NextFlowUID
import backend.flowNetwork.{FlowNode, FlowFieldOfInterest, TargetableFlow}
import backend.flowTypes.{FlowObject, WordObject}

object FlowTokenizer {
  var nodeType = "Tokenizer"
  def props(id:Long, name: String,  x: Int, y: Int): Props = Props(new FlowTokenizer(id, name, x, y))
}

class FlowTokenizer(id: Long, name: String,  x: Int, y: Int)
  extends FlowNode(id, name, FlowTokenizer.nodeType, x, y, 1 ,1) with TargetableFlow with FlowFieldOfInterest {

  var separator: String = " "

  addConfigMapGetters(() => Map(
    "separator" -> separator,
    "display" -> "separator"
  ))

  addConfigSetters({
    case ("separator", sep) =>
      log.info(s"Updating separator to $sep")
      separator = sep
  })

  override def active: Receive = {
    case o: FlowObject =>
      o.contentAsString(fieldOfInterest) match {
        case Some(content) => content.split(separator).foreach(target ! WordObject(NextFlowUID(), o, _))
        case None => log.debug(s"Message ${o.uid} doesn't have a String convertible field $fieldOfInterest")
      }
  }
}