package sych.ad.bot.handler.handlers

import cats.effect.IO
import org.typelevel.log4cats.LoggerFactory
import sych.ad.bot.handler.{Reaction, SendMessage}
import telegramium.bots.{ChatIntId, Message}

trait Help {
  def handle(msg: Message): IO[List[Reaction]]
}

object Help {
  def apply(implicit L: LoggerFactory[IO]): Help = { msg =>
    val logger = L.getLogger
    val chatId = msg.chat.id
    msg.from match {
      case Some(user) =>
        val userId = user.id
        for {
          _ <- logger.info(s"received from chatId=$chatId, userId=$userId command=/help")
          result = List(
            SendMessage(
              ChatIntId(chatId),
              "/start - Initialize the bot or session\n\n" +
                "/help - Show list of available commands \n\n" +
                "/track - Add link to the tracking list \n\n" +
                "/untrack - Delete link from the tracking list\n\n" +
                "/list - See list of currently tracking links"
            )
          )
        } yield result
      case None =>
        for {
          _ <- logger.warn(s"received /help without user info from chatId=$chatId")
        } yield List(SendMessage(ChatIntId(chatId), "Cannot identify user"))
    }
  }
}
