package sych.ad.ai.kafka

import cats.effect._
import fs2.kafka._
import sych.ad.ai.config.KafkaConsumerConfig
import sych.ad.ai.server.AIService
import sych.ad.common.dto.LinkUpdate
import tethys._
import tethys.jackson._

trait AIConsumer {
  def consume(key: Long, value: LinkUpdate): IO[Unit]
}

object AIConsumer {

  private class Impl(aiService: AIService) extends AIConsumer {
    override def consume(key: Long, value: LinkUpdate): IO[Unit] =
      aiService.processLinkUpdate(value)
  }

  implicit val keyDeserializer: Deserializer[IO, Long] = Deserializer.long

  implicit val valueDeserializer: Deserializer[IO, Either[Throwable, LinkUpdate]] =
    Deserializer.string[IO].map(_.jsonAs[LinkUpdate])

  def run(
      aiService: AIService,
      config: KafkaConsumerConfig
  ): Resource[IO, Unit] = {
    val consumer = new Impl(aiService)
    KafkaConsumer
      .stream(ConsumerSettings[IO, Long, Either[Throwable, LinkUpdate]].withProperties(config.properties))
      .subscribeTo(config.topic)
      .records
      .evalMap { committable =>
        committable.record.value match {
          case Left(_)      => IO.unit
          case Right(value) => consumer.consume(committable.record.key, value)
        }
      }
      .compile
      .drain
      .toResource
  }
}
