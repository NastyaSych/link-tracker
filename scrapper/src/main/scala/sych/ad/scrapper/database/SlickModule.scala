package sych.ad.scrapper.database

import cats.effect.{IO, Resource}
import slick.jdbc.PostgresProfile.api._
import sych.ad.scrapper.config.DatabaseConfig

object SlickModule {
  def resource(dbConfig: DatabaseConfig): Resource[IO, Database] = {
    Resource.make(IO {
      val ds = DatabaseConfig.toDataSource(dbConfig)
      Database.forDataSource(ds, None)
    })(db => IO(db.close()))
  }
}
