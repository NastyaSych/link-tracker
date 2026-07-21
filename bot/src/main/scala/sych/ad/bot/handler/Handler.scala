package sych.ad.bot.handler

import cats.effect.IO
import org.typelevel.log4cats.LoggerFactory
import sych.ad.bot.client.ScrapperClient
import sych.ad.bot.handler.handlers.{Help, ShowList, Start, Track, Unknown, Untrack}
import telegramium.bots.Message

trait Handler {
  def onMessage(message: Message): IO[List[Reaction]]
}

class HandlerImpl(scrapperClient: ScrapperClient, botUsername: String)(implicit val L: LoggerFactory[IO])
  extends Handler {

  private val logger = L.getLogger

  private val start: Start                                 = Start(scrapperClient)(L)
  private val help: Help                                   = Help(L)
  private val track: Track                                 = Track(scrapperClient)(L)
  private val untrack: Untrack                             = Untrack(scrapperClient)(L)
  private val showList: ShowList                           = ShowList(scrapperClient)(L)
  private val unknown: Unknown                             = Unknown(L)
  override def onMessage(msg: Message): IO[List[Reaction]] = {
    for {
      _        <- logger.info(s"handling from chatId=${msg.chat.id} message=${msg.text.getOrElse("")}")
      reaction <- msg.text match {
        case Some(text) =>
          val clean = text.replaceAll(s"@$botUsername", "").trim
          clean match {
            case "/start"          => start.handle(msg)
            case "/help"           => help.handle(msg)
            case s"/track $link"   => track.handle(msg, link)
            case s"/untrack $link" => untrack.handle(msg, link)
            case "/list"           => showList.handle(msg)
            case _                 => unknown.handle(msg)
          }
        case None => unknown.handle(msg)
      }
    } yield reaction
  }
}
