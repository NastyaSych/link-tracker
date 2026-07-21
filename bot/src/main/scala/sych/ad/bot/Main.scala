package sych.ad.bot

import cats.effect.{ExitCode, IO, IOApp, Resource}
import org.http4s.ember.client.EmberClientBuilder
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory
import sych.ad.bot.bot.MyBot
import sych.ad.bot.client.ScrapperClient
import sych.ad.bot.config.BotConfig
import sych.ad.bot.handler.HandlerImpl
import sych.ad.bot.kafka.LinkUpdateConsumer
import sych.ad.bot.server.{BotController, BotServer, BotServiceImpl}
import telegramium.bots.high._

object Main extends IOApp {
  implicit val logging: LoggerFactory[IO] = Slf4jFactory.create[IO]
  private val logger                      = logging.getLogger

  override def run(args: List[String]): IO[ExitCode] = {
    val resources = for {
      config         <- Resource.eval(BotConfig.load)
      scrapperClient <- ScrapperClient.resource(config.scrapper, config.httpClient)
      httpClient     <- EmberClientBuilder.default[IO].build
    } yield (config, scrapperClient, httpClient)

    resources.map { case (config, scrapperClient, httpClient) =>
      val botUsername           = config.tgBot.botName
      implicit val api: Api[IO] = BotApi(httpClient, s"${config.tgBot.baseUrl}${config.tgBot.token}")
      val handler               = new HandlerImpl(scrapperClient, botUsername)
      val bot                   = new MyBot(handler)

      (bot, config)
    }.use { case (bot, config) =>
      for {
        _ <- bot.setMenu()
        _ <- logger.info("Bot menu set")
        _ <- bot.start().start
        botService    = new BotServiceImpl(bot)
        botController = BotController(botService)
        botServer     = BotServer(config.httpServer, botController)
        // updateConsumer = Resource.unit[IO] //
        updateConsumer = LinkUpdateConsumer.run(botService, config.linkUpdateConsumer)
        _ <- (botServer.run, updateConsumer).parTupled.use(_ => IO.never).start
      } yield ExitCode.Success
    }.flatMap(_ => IO.never)
  }
}
