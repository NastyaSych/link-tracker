package sych.ad.common.dto

import sttp.tapir.Schema
import tethys.derivation.semiauto._
import tethys.{JsonReader, JsonWriter}

case class RemoveLinkRequest(
    link: String
)
object RemoveLinkRequest {
  implicit val schema: Schema[RemoveLinkRequest] =
    Schema.derived[RemoveLinkRequest]

  implicit val writer: JsonWriter[RemoveLinkRequest] = jsonWriter[RemoveLinkRequest]

  implicit val reader: JsonReader[RemoveLinkRequest] = jsonReader[RemoveLinkRequest]
}
