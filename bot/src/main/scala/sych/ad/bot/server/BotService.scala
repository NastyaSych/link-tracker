package sych.ad.bot.server

import cats.effect.IO
import cats.implicits._
import org.typelevel.log4cats.LoggerFactory
import sych.ad.bot.bot.MyBot
import sych.ad.bot.handler.SendMessage
import sych.ad.common.dto.LinkUpdate
import telegramium.bots._

trait BotService {
  def handleLinkUpdate(update: LinkUpdate): IO[Unit]
}

class BotServiceImpl(
    telegramBot: MyBot
)(implicit L: LoggerFactory[IO]) extends BotService {

  private val logger                                          = L.getLogger
  override def handleLinkUpdate(update: LinkUpdate): IO[Unit] =
    for {
      _ <- logger.info(s"Received link update: ${update.url}")
      _ <- update.tgChatIds.parTraverse_ { chatId =>
        val message   = formatLinkUpdateMessage(update)
        val reactions = List(SendMessage(ChatIntId(chatId), message))
        telegramBot.sendReactions(reactions)
      }
    } yield ()

  private def formatLinkUpdateMessage(update: LinkUpdate): String = {
    val header   = "Link updated!"
    val urlLine  = s"URL: ${update.url}"
    val descLine = s"Description: ${update.description}"

    s"$header\n\n$urlLine\n$descLine"
  }
}

object BotService {
  def apply(telegramBot: MyBot)(implicit L: LoggerFactory[IO]): BotService =
    new BotServiceImpl(telegramBot)
}
