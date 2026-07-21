package sych.ad.scrapper.notification

import cats.effect.IO
import org.typelevel.log4cats.LoggerFactory
import sych.ad.common.dto.LinkUpdate
import sych.ad.scrapper.kafka.LinkUpdateProducer

class KafkaMessageSender(
    linkUpdateProducer: LinkUpdateProducer
)(implicit L: LoggerFactory[IO]) extends MessageSender {

  private val logger = L.getLogger

  override def sendUpdate(linkUpdate: LinkUpdate): IO[Unit] = {
    for {
      _ <- logger.info(s"Sending KAFKA update to users ${linkUpdate.tgChatIds} for link ${linkUpdate.url}")
      _ <- linkUpdateProducer.produce(linkUpdate)
    } yield ()
  }
}

object KafkaMessageSender {
  def apply(linkUpdateProducer: LinkUpdateProducer)(implicit L: LoggerFactory[IO]): KafkaMessageSender =
    new KafkaMessageSender(linkUpdateProducer)
}
