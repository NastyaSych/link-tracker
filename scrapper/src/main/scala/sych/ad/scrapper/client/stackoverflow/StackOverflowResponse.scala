package sych.ad.scrapper.client.stackoverflow

import sttp.tapir.Schema
import tethys._
import tethys.derivation.semiauto.{jsonReader, jsonWriter}

import java.time.Instant

case class Owner(display_name: String)

object Owner {
  implicit val ownerSchema: Schema[Owner]     = Schema.derived[Owner]
  implicit val ownerReader: JsonReader[Owner] = JsonReader.builder
    .addField[String]("display_name")
    .buildReader(Owner.apply)
  implicit val ownerWriter: JsonWriter[Owner] = jsonWriter[Owner]
}

case class Item(
    creation_date: Long,
    owner: Owner,
    body: String
) {
  def createdAt: Instant = Instant.ofEpochSecond(creation_date)
}

object Item {
  implicit val itemSchema: Schema[Item]     = Schema.derived[Item]
  implicit val itemReader: JsonReader[Item] = JsonReader.builder
    .addField[Long]("creation_date")
    .addField[Owner]("owner")
    .addField[String]("body")
    .buildReader(Item.apply)
  implicit val itemWriter: JsonWriter[Item] = jsonWriter[Item]
}

case class Response(items: List[Item])

object Response {
  implicit val responseSchema: Schema[Response]     = Schema.derived[Response]
  implicit val responseReader: JsonReader[Response] = jsonReader[Response]
  implicit val responseWriter: JsonWriter[Response] = jsonWriter[Response]
}

case class QuestionTitle(
    title: String
)

object QuestionTitle {
  implicit val schema: Schema[QuestionTitle]          = Schema.derived[QuestionTitle]
  implicit val titleReader: JsonReader[QuestionTitle] = JsonReader.builder
    .addField[String]("title")
    .buildReader(QuestionTitle.apply)
  implicit val titleWriter: JsonWriter[QuestionTitle] = jsonWriter[QuestionTitle]
}

case class QuestionResponse(items: List[QuestionTitle])

object QuestionResponse {
  implicit val questionResponseSchema: Schema[QuestionResponse]     = Schema.derived[QuestionResponse]
  implicit val questionResponseReader: JsonReader[QuestionResponse] = jsonReader[QuestionResponse]
  implicit val questionResponseWriter: JsonWriter[QuestionResponse] = jsonWriter[QuestionResponse]
}
