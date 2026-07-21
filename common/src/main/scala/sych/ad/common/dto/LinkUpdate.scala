package sych.ad.common.dto

import sttp.tapir.Schema
import tethys.derivation.semiauto._
import tethys.{JsonReader, JsonWriter}

case class LinkUpdate(
    id: Long,
    url: String,
    description: String,
    tgChatIds: List[Long]
)

object LinkUpdate {
  implicit val schema: Schema[LinkUpdate] =
    Schema.derived[LinkUpdate]

  implicit val writer: JsonWriter[LinkUpdate] = jsonWriter[LinkUpdate]

  implicit val reader: JsonReader[LinkUpdate] = jsonReader[LinkUpdate]
}
