package sych.ad.scrapper.notification

import pureconfig.ConfigReader
import pureconfig.error.UserValidationFailed

sealed trait MessageSenderMode

object MessageSenderMode {
  case object Kafka extends MessageSenderMode

  case object Http extends MessageSenderMode

  implicit val messageSenderModeConfigReader: ConfigReader[MessageSenderMode] =
    ConfigReader[String].emap[MessageSenderMode] { name =>
      name.toLowerCase match {
        case "kafka" => Right(Kafka)
        case "http"  => Right(Http)
        case _       => Left(UserValidationFailed("Unknown message-sender-mode"))
      }
    }
}
