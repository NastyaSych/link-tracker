package sych.ad.ai.config

import cats.effect.IO
import pureconfig.ConfigSource
import pureconfig.generic.auto._

case class AIProcessingConfig(
    maxLength: Int,
    blockedAuthors: List[String],
    stopWords: List[String]
)

case class AIConfig(
    aiProcessing: AIProcessingConfig,
    aiConsumer: KafkaConsumerConfig,
    aiProducer: KafkaProducerConfig,
)

object AIConfig {
  def load: IO[AIConfig] =
    IO.delay(ConfigSource.default.loadOrThrow[AIConfig])
}
