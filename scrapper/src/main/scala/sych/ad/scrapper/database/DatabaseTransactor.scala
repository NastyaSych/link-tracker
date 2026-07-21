package sych.ad.scrapper.database

import cats.effect.{IO, Resource}
import doobie.Transactor
import doobie.hikari.HikariTransactor
import sych.ad.scrapper.config.DatabaseConfig

final case class DatabaseTransactor(
    transactor: Transactor[IO]
)

object DatabaseTransactor {
  def resource(config: DatabaseConfig): Resource[IO, DatabaseTransactor] = {
    val hikariConfig = DatabaseConfig.toHikariConf(config)
    for {
      xa <- HikariTransactor.fromHikariConfig[IO](hikariConfig)
    } yield DatabaseTransactor(xa)
  }
}
