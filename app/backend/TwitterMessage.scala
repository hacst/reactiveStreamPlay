package backend

import shapeless._
import syntax.typeable._

case class TwitterMessage(override val uid: Long, message: String) extends FlowObject {
  override def content(field: String): Option[Any] = field match {
    case "default" | "message" => Some(message)
    case _ => None
  }

  override def fields(): List[String] = List(
    "default",
    "message"
  )

  override val originUid: Long = -1

}
