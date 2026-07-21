package sych.ad.scrapper.kafka

import cats.effect._
import fs2.kafka.{KafkaProducer, ProducerRecord, ProducerSettings, Serializer}
import sych.ad.common.dto.LinkUpdate
import sych.ad.scrapper.config.KafkaProducerConfig
import tethys._
import tethys.jackson._

import java.nio.charset.StandardCharsets

trait LinkUpdateProducer {
  def produce(linkUpdate: LinkUpdate): IO[Unit]
}

object LinkUpdateProducer {

  private class Impl(topic: String, kafkaProducer: KafkaProducer[IO, Long, LinkUpdate])
    extends LinkUpdateProducer {
    override def produce(linkUpdate: LinkUpdate): IO[Unit] =
      kafkaProducer
        .produceOne(ProducerRecord(topic, linkUpdate.id, linkUpdate))
        .flatten
        .void
  }

  implicit val serializerLinkUpdate: Serializer[IO, LinkUpdate] =
    Serializer.string[IO](StandardCharsets.UTF_8).contramap(_.asJson)

  implicit val serializerKeyLinkUpdate: Serializer[IO, Long] = Serializer.long

  def make(config: KafkaProducerConfig): Resource[IO, LinkUpdateProducer] =
    KafkaProducer
      .resource(ProducerSettings[IO, Long, LinkUpdate].withProperties(config.properties))
      .map(producer => new Impl(config.topic, producer))
}
