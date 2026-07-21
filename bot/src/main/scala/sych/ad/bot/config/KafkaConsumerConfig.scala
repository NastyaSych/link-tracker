package sych.ad.bot.config

import pureconfig.ConfigReader

final case class KafkaConsumerConfig(
    topic: String,
    properties: Map[String, String]
)

object KafkaConsumerConfig {
  implicit val configReader: ConfigReader[KafkaConsumerConfig] = pureconfig.generic.semiauto.deriveReader
}
