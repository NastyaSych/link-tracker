package sych.ad.bot.handler.handlers

import cats.effect.IO
import org.typelevel.log4cats.LoggerFactory
import sych.ad.bot.handler.{Reaction, SendMessage}
import telegramium.bots.{ChatIntId, Message}

trait Unknown {
  def handle(msg: Message): IO[List[Reaction]]
}

object Unknown {
  def apply(implicit L: LoggerFactory[IO]): Unknown = { msg =>
    val logger = L.getLogger
    val chatId = msg.chat.id
    msg.from match {
      case Some(user) =>
        val userId = user.id
        for {
          _ <- logger.error(
            s"received from chatId=$chatId, userId=$userId unknown command=${msg.text.getOrElse("")}"
          )
          result = List(
            SendMessage(
              ChatIntId(chatId),
              "Unknown command. Use /help or menu to see list of available commands"
            )
          )
        } yield result
      case None =>
        for {
          _ <- logger.warn(s"received unknown command without user info from chatId=$chatId")
        } yield List(SendMessage(ChatIntId(chatId), "Cannot identify user"))
    }
  }
}
