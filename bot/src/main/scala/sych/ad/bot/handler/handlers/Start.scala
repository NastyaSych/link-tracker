package sych.ad.bot.handler.handlers

import cats.effect.IO
import org.typelevel.log4cats.LoggerFactory
import sych.ad.bot.client.ScrapperClient
import sych.ad.bot.handler.{Reaction, SendMessage}
import telegramium.bots.{ChatIntId, Message}

trait Start {
  def handle(msg: Message): IO[List[Reaction]]
}

object Start {
  def apply(client: ScrapperClient)(implicit L: LoggerFactory[IO]): Start = { msg =>
    val logger = L.getLogger
    val chatId = msg.chat.id
    msg.from match {
      case Some(user) =>
        val userId = user.id
        for {
          _ <- logger.info(s"received from chatId=$chatId, userId=$userId command=/start")
          _ <- client.registerChat(chatId, userId)
          result = List(
            SendMessage(
              ChatIntId(chatId),
              "Welcome! Use /help or menu to see list of available commands"
            )
          )
        } yield result
      case None =>
        for {
          _ <- logger.warn(s"received /start without user info from chatId=$chatId")
        } yield List(SendMessage(ChatIntId(chatId), "Cannot identify user"))
    }
  }
}
