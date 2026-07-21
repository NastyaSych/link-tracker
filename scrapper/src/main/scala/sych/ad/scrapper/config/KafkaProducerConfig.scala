package sych.ad.scrapper.config

import pureconfig.ConfigReader

final case class KafkaProducerConfig(
    topic: String,
    properties: Map[String, String]
)

object KafkaProducerConfig {
  implicit val configReader: ConfigReader[KafkaProducerConfig] = pureconfig.generic.semiauto.deriveReader
}
