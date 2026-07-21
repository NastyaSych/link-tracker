package sych.ad.scrapper.config

import pureconfig.ConfigReader
import pureconfig.generic.semiauto.deriveReader

import scala.concurrent.duration.FiniteDuration

case class ValkeyConfig(
    uri: String,
    ttl: Option[FiniteDuration]
)

object ValkeyConfig {
  implicit val configReader: ConfigReader[ValkeyConfig] = deriveReader[ValkeyConfig]
}
