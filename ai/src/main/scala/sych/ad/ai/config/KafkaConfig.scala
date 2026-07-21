package sych.ad.ai.config

import pureconfig.ConfigReader

final case class KafkaConsumerConfig(
    topic: String,
    properties: Map[String, String]
)

object KafkaConsumerConfig {
  implicit val configReader: ConfigReader[KafkaConsumerConfig] = pureconfig.generic.semiauto.deriveReader
}

final case class KafkaProducerConfig(
    topic: String,
    properties: Map[String, String]
)

object KafkaProducerConfig {
  implicit val configReader: ConfigReader[KafkaProducerConfig] = pureconfig.generic.semiauto.deriveReader
}
