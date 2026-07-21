package sych.ad.bot.handler.handlers

import cats.effect.IO
import org.typelevel.log4cats.LoggerFactory
import sych.ad.bot.client.ScrapperClient
import sych.ad.bot.handler.{Reaction, SendMessage}
import sych.ad.common.dto.RemoveLinkRequest
import telegramium.bots.{ChatIntId, Message}

trait Untrack {
  def handle(msg: Message, link: String): IO[List[Reaction]]
}

object Untrack {
  def apply(client: ScrapperClient)(implicit L: LoggerFactory[IO]): Untrack = { (msg, link) =>
    val logger = L.getLogger
    val chatId = msg.chat.id
    msg.from match {
      case Some(user) =>
        val userId = user.id
        for {
          _        <- logger.info(s"received from chatId=$chatId, userId=$userId command=/untrack")
          response <- client.deleteLink(chatId, userId, RemoveLinkRequest(link))
          result = if (response.wasBefore) {
            List(SendMessage(ChatIntId(chatId), "Link was deleted"))
          } else {
            List(SendMessage(ChatIntId(chatId), "Link wasn't found"))
          }
        } yield result
      case None =>
        for {
          _ <- logger.warn(s"received /untrack without user info from chatId=$chatId")
        } yield List(SendMessage(ChatIntId(chatId), "Cannot identify user"))
    }
  }
}
