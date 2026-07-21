package sych.ad.scrapper.valkey

import cats.effect.{IO, Resource}
import dev.profunktor.redis4cats.data.RedisCodec
import dev.profunktor.redis4cats.effect.Log.NoOp._
import dev.profunktor.redis4cats.{Redis, RedisCommands}
import io.lettuce.core.{ClientOptions, TimeoutOptions}
import sych.ad.scrapper.config.ValkeyConfig

import java.time.Duration
import scala.concurrent.duration.FiniteDuration

trait ValkeyCache[K, V] {
  val commands: RedisCommands[IO, K, V]
  val ttl: Option[FiniteDuration]

  def get(key: K): IO[Option[V]] = commands.get(key)

  def set(key: K, value: V): IO[Unit] =
    ttl.fold(
      commands.set(key, value)
    )(exp => commands.setEx(key, value, exp))

  def del(key: K): IO[Unit] = commands.del(key).void
}

object ValkeyCache {

  def makeRedisCommands[K, V](
      config: ValkeyConfig,
      codec: RedisCodec[K, V]
  ): Resource[IO, RedisCommands[IO, K, V]] = {
    val clientOptions = ClientOptions.builder()
      .autoReconnect(false)
      .pingBeforeActivateConnection(true)
      .timeoutOptions(TimeoutOptions.builder()
        .fixedTimeout(Duration.ofSeconds(10))
        .build())
      .build()

    Redis[IO].withOptions(config.uri, clientOptions, codec)
  }
}
