package sych.ad.scrapper.valkey

import cats.effect.{IO, Resource}
import dev.profunktor.redis4cats.RedisCommands
import dev.profunktor.redis4cats.codecs.Codecs
import dev.profunktor.redis4cats.codecs.splits.stringLongEpi
import dev.profunktor.redis4cats.data.RedisCodec
import sych.ad.common.dto.ListLinksResponse
import sych.ad.scrapper.config.ValkeyConfig

import scala.concurrent.duration.FiniteDuration

object GetLinksCache {

  private val codec: RedisCodec[Long, ListLinksResponse] =
    Codecs.derive(RedisCodec.Utf8, stringLongEpi, ListLinksResponse.epi)

  def make(config: ValkeyConfig): Resource[IO, ValkeyCache[Long, ListLinksResponse]] =
    ValkeyCache.makeRedisCommands(config, codec).map { redisCommands =>
      new ValkeyCache[Long, ListLinksResponse] {
        val commands: RedisCommands[IO, Long, ListLinksResponse] = redisCommands
        val ttl: Option[FiniteDuration]                          = config.ttl
      }
    }
}
