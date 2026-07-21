package sych.ad.scrapper.config

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import pureconfig.ConfigReader

final case class DatabaseConfig(
    accessType: String,
    driver: String,
    url: String,
    user: String,
    password: String
)

object DatabaseConfig {
  implicit val configReader: ConfigReader[DatabaseConfig] = pureconfig.generic.semiauto.deriveReader

  def toHikariConf(config: DatabaseConfig): HikariConfig = {
    val hikariConfig = new HikariConfig()
    hikariConfig.setDriverClassName(config.driver)
    hikariConfig.setJdbcUrl(config.url)
    hikariConfig.setUsername(config.user)
    hikariConfig.setPassword(config.password)
    hikariConfig
  }

  def toDataSource(config: DatabaseConfig): HikariDataSource = {
    val ds = new HikariDataSource()
    ds.setDriverClassName(config.driver)
    ds.setJdbcUrl(config.url)
    ds.setUsername(config.user)
    ds.setPassword(config.password)
    ds
  }
}
