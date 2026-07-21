package sych.ad.common.dto

import cats.effect.IO
import sttp.tapir.Schema
import tethys.derivation.semiauto._
import tethys.{JsonReader, JsonWriter}

case class ApiErrorResponse(
    description: String,
    code: String,
    exceptionName: String,
    exceptionMessage: String,
    stacktrace: List[String]
)

object ApiErrorResponse {
  implicit val schema: Schema[ApiErrorResponse] =
    Schema.derived[ApiErrorResponse]

  implicit val writer: JsonWriter[ApiErrorResponse] = jsonWriter[ApiErrorResponse]

  implicit val reader: JsonReader[ApiErrorResponse] = jsonReader[ApiErrorResponse]

  implicit class IOWithApiError[A](private val io: IO[A]) extends AnyVal {

    def withApiError(description: String = "Default API error", code: Int = 500): IO[A] =
      io.handleErrorWith { th =>
        val error = ApiErrorResponse(
          description,
          code.toString,
          th.getClass.getSimpleName,
          Option(th.getMessage).getOrElse("No message"),
          th.getStackTrace.map(_.toString).toList
        )
        IO.raiseError(new RuntimeException(error.toString))
      }

    def toApiError: IO[Either[ApiErrorResponse, A]] =
      io.attempt.map {
        case Right(value) => Right(value)
        case Left(th)     => Left(
            ApiErrorResponse(
              "Default API error",
              "500",
              th.getClass.getSimpleName,
              Option(th.getMessage).getOrElse("No message"),
              th.getStackTrace.map(_.toString).toList
            )
          )
      }
  }
}
