package sych.ad.scrapper.notification

import cats.effect.IO
import org.typelevel.log4cats.LoggerFactory
import sych.ad.common.dto.LinkUpdate
import sych.ad.scrapper.client.bot.BotClient

class HttpMessageSender(
    botClient: BotClient,
    fallback: MessageSender
)(implicit L: LoggerFactory[IO]) extends MessageSender {

  private val logger = L.getLogger

  override def sendUpdate(linkUpdate: LinkUpdate): IO[Unit] = {
    for {
      _ <- logger.info(s"Sending HTTP update to users ${linkUpdate.tgChatIds} for link ${linkUpdate.url}")
      _ <- botClient.sendLinkUpdate(linkUpdate).handleErrorWith { err =>
        logger
          .warn(s"Fallback Kafka event after error = $err")
          .flatMap(_ => fallback.sendUpdate(linkUpdate))
      }
    } yield ()
  }
}

object HttpMessageSender {
  def apply(botClient: BotClient, fallback: MessageSender)(implicit L: LoggerFactory[IO]): HttpMessageSender =
    new HttpMessageSender(botClient, fallback)
}
