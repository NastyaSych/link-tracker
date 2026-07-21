package sych.ad.common.dto

import dev.profunktor.redis4cats.codecs.splits.SplitEpi
import sttp.tapir.Schema
import tethys._
import tethys.derivation.semiauto._
import tethys.jackson._

final case class ListLinksResponse(
    links: List[LinkResponse],
    size: Int
)

object ListLinksResponse {

  val default: ListLinksResponse = ListLinksResponse(List.empty, 0)

  val epi: SplitEpi[String, ListLinksResponse] = SplitEpi(x => x.jsonAs.getOrElse(default), _.asJson)

  implicit val schema: Schema[ListLinksResponse] =
    Schema.derived[ListLinksResponse]

  implicit val writer: JsonWriter[ListLinksResponse] = jsonWriter[ListLinksResponse]

  implicit val reader: JsonReader[ListLinksResponse] = jsonReader[ListLinksResponse]
}
