package sych.ad.common.dto

import sttp.tapir.Schema
import tethys.derivation.semiauto._
import tethys.{JsonReader, JsonWriter}

case class LinkResponse(
    id: Long,
    url: String,
    wasBefore: Boolean
)
object LinkResponse {
  implicit val schema: Schema[LinkResponse] =
    Schema.derived[LinkResponse]

  implicit val writer: JsonWriter[LinkResponse] = jsonWriter[LinkResponse]

  implicit val reader: JsonReader[LinkResponse] = jsonReader[LinkResponse]
}
