package sych.ad.bot.handler.handlers

import cats.effect.IO
import org.typelevel.log4cats.LoggerFactory
import sych.ad.bot.client.ScrapperClient
import sych.ad.bot.handler.{Reaction, SendMessage}
import sych.ad.common.dto.AddLinkRequest
import telegramium.bots.{ChatIntId, Message}

trait Track {
  def handle(msg: Message, link: String): IO[List[Reaction]]
}

object Track {
  def apply(client: ScrapperClient)(implicit L: LoggerFactory[IO]): Track = { (msg, link) =>
    val logger = L.getLogger
    val chatId = msg.chat.id
    msg.from match {
      case Some(user) =>
        val userId  = user.id
        val linkAdd = new AddLinkRequest(link)
        for {
          _ <- logger.info(s"received from chatId=$chatId, userId=$userId command=/track")
          _ <- client.addLink(chatId, userId, linkAdd)
          result = List(SendMessage(ChatIntId(chatId), "Link was added"))
        } yield result
      case None =>
        for {
          _ <- logger.warn(s"received /track without user info from chatId=$chatId")
        } yield List(SendMessage(ChatIntId(chatId), "Cannot identify user"))
    }
  }
}
