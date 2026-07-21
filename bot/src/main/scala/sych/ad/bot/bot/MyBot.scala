package sych.ad.bot.bot

import cats.effect.IO
import org.typelevel.log4cats.LoggerFactory
import sych.ad.bot.handler.{Handler, Reaction, SendMessage}
import telegramium.bots.high.implicits._
import telegramium.bots.high.{Api, LongPollBot, Methods}
import telegramium.bots.{BotCommand, Message}

class MyBot(handler: Handler)(implicit api: Api[IO], L: LoggerFactory[IO])
  extends LongPollBot[IO](api) {
  private val logger      = L.getLogger
  def setMenu(): IO[Unit] = {
    for {
      success <- Methods.setMyCommands(
        commands = List(
          BotCommand("/start", "Kick it off, let's roll"),
          BotCommand("/help", "You require my assistance?"),
          BotCommand("/track", "Put link in the trunk, Christofah"),
          BotCommand("/untrack", "Hasta la vista, linky"),
          BotCommand("/list", "One list to rule them all")
        )
      ).exec
      _ <- if (success)
        logger.info("menu setup completed successfully")
      else
        logger.error("menu setup failed")
    } yield ()
  }

  override def onMessage(msg: Message): IO[Unit] = {
    for {
      _         <- logger.info(s"processing from chatId=${msg.chat.id} message=${msg.text.getOrElse("")} ")
      reactions <- handler.onMessage(msg)
      _         <- sendReactions(reactions)
    } yield ()
  }

  def sendReactions(reactions: List[Reaction]): IO[Unit] = {
    reactions.foldLeft(IO.unit) { (acc, reaction) =>
      reaction match {
        case SendMessage(chatId, text) =>
          for {
            _ <- acc
            _ <- logger.debug(s"sending to chatId=${chatId.id} message=$text")
            _ <- Methods.sendMessage(chatId, text).exec.void
          } yield ()
      }
    }
  }
}
