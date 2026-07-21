package sych.ad.bot.kafka

import cats.effect._
import fs2.kafka._
import sych.ad.bot.config.KafkaConsumerConfig
import sych.ad.bot.server.BotService
import sych.ad.common.dto.LinkUpdate
import tethys._
import tethys.jackson._

trait LinkUpdateConsumer {
  def consume(key: Long, value: LinkUpdate): IO[Unit]
}

object LinkUpdateConsumer {

  private class Impl(botService: BotService) extends LinkUpdateConsumer {
    override def consume(key: Long, value: LinkUpdate): IO[Unit] =
      botService.handleLinkUpdate(value)
  }

  implicit val keyDeserializer: Deserializer[IO, Long] = Deserializer.long

  implicit val valueDeserializer: Deserializer[IO, Either[Throwable, LinkUpdate]] =
    Deserializer.string[IO].map(_.jsonAs[LinkUpdate])

  def run(
      botService: BotService,
      config: KafkaConsumerConfig
  ): Resource[IO, Unit] = {
    val consumer = new Impl(botService)
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
