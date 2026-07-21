package sych.ad.bot.handler.handlers

import cats.effect.IO
import org.typelevel.log4cats.LoggerFactory
import sych.ad.bot.client.ScrapperClient
import sych.ad.bot.handler.{Reaction, SendMessage}
import telegramium.bots.{ChatIntId, Message}

trait ShowList {
  def handle(msg: Message): IO[List[Reaction]]
}

object ShowList {
  def apply(client: ScrapperClient)(implicit L: LoggerFactory[IO]): ShowList = { msg =>
    val logger = L.getLogger
    val chatId = msg.chat.id
    msg.from match {
      case Some(user) =>
        val userId = user.id
        for {
          _        <- logger.info(s"received from chatId=$chatId, userId=$userId command=/list")
          response <- client.getLinks(chatId, userId)
          links  = response.links.map(_.url)
          result =
            List(
              SendMessage(
                ChatIntId(chatId),
                if (links.isEmpty) "No links in list yet"
                else links.zipWithIndex.map { case (link, i) => s"${i + 1}. $link" }.mkString("\n")
              )
            )
        } yield result
      case None =>
        for {
          _ <- logger.warn(s"received /list without user info from chatId=$chatId")
        } yield List(SendMessage(ChatIntId(chatId), "Cannot identify user"))
    }
  }
}
