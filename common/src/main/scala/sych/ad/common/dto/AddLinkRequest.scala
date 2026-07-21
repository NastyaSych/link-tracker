package sych.ad.common.dto

import sttp.tapir.Schema
import tethys._
import tethys.derivation.semiauto._

case class AddLinkRequest(
    link: String,
    tags: List[String] = List.empty,
    filters: List[String] = List.empty
)
object AddLinkRequest {
  implicit val schema: Schema[AddLinkRequest] =
    Schema.derived[AddLinkRequest]

  implicit val writer: JsonWriter[AddLinkRequest] = jsonWriter[AddLinkRequest]

  implicit val reader: JsonReader[AddLinkRequest] = jsonReader[AddLinkRequest]
}
