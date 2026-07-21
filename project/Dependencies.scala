import sbt.*

object Dependencies {
  // cats
  val catsCore   = "org.typelevel" %% "cats-core"   % "2.13.0"
  val catsEffect = "org.typelevel" %% "cats-effect" % "3.5.7"

  // tapir
  val tapirVersion = "1.11.13"

  val tapirHttp4s     = "com.softwaremill.sttp.tapir" %% "tapir-http4s-server"     % tapirVersion
  val tapirSwagger    = "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion
  val tapirTethys     = "com.softwaremill.sttp.tapir" %% "tapir-json-tethys"       % tapirVersion
  val tapirSttpClient = "com.softwaremill.sttp.tapir" %% "tapir-sttp-client"       % tapirVersion

  // http4s
  val http4sVersion = "0.23.30"

  val http4sServer = "org.http4s" %% "http4s-ember-server" % http4sVersion
  val http4sClient = "org.http4s" %% "http4s-ember-client" % http4sVersion
  val http4sDsl    = "org.http4s" %% "http4s-dsl"          % http4sVersion

  // sttp
  val sttpVersion = "3.10.2"

  val sttpCore = "com.softwaremill.sttp.client3" %% "core" % sttpVersion
  val sttpCats = "com.softwaremill.sttp.client3" %% "cats" % sttpVersion

  // logback
  val logback  = "ch.qos.logback" % "logback-classic" % "1.5.20"
  val log4cats = "org.typelevel" %% "log4cats-core"   % "2.7.0"
  val logslf   = "org.typelevel" %% "log4cats-slf4j"  % "2.7.0"

  // tethys
  val tethysVersion = "0.29.3"

  val tethysCore       = "com.tethys-json" %% "tethys-core"       % tethysVersion
  val tethysJackson    = "com.tethys-json" %% "tethys-jackson213" % tethysVersion
  val tethysDerivation = "com.tethys-json" %% "tethys-derivation" % tethysVersion

  // pureconfig
  val pureConfigVersion = "0.17.10"

  val pureConfig     = "com.github.pureconfig" %% "pureconfig"      % pureConfigVersion
  val pureConfigCore = "com.github.pureconfig" %% "pureconfig-core" % pureConfigVersion

  // telegramium
  val telegramiumVersion = "10.904.0"

  val telegramiumCore = "io.github.apimorphism" %% "telegramium-core" % telegramiumVersion
  val telegramiumHigh = "io.github.apimorphism" %% "telegramium-high" % telegramiumVersion

  // scheduler
  val quartzScheduler = "com.itv" %% "quartz4s-core" % "1.0.4"

  // circuit breaker
  val circuit = "io.chrisdavenport" %% "circuit" % "0.5.1"

  // db
  val `doobie-version`     = "1.0.0-RC12"
  val `liquibase-version`  = "5.0.1"
  val `h2-version`         = "2.4.240"
  val `redis4cats-version` = "2.0.3"

  val db: List[ModuleID] = List(
    // doobie
    "org.tpolecat" %% "doobie-core"     % `doobie-version`,
    "org.tpolecat" %% "doobie-postgres" % `doobie-version`,
    "org.tpolecat" %% "doobie-h2"       % `doobie-version`,
    "org.tpolecat" %% "doobie-hikari"   % `doobie-version`,

    // h2
    "com.h2database" % "h2" % `h2-version`,

    // liquibase
    "org.liquibase" % "liquibase-core" % `liquibase-version`,

    // orm
    "com.typesafe.slick" %% "slick"          % "3.5.0",
    "com.typesafe.slick" %% "slick-hikaricp" % "3.5.0",
    "org.postgresql"      % "postgresql"     % "42.5.0",
    "com.kubukoz"        %% "slick-effect"   % "0.6.0",

    // redis
    "dev.profunktor" %% "redis4cats-effects"  % `redis4cats-version`,
    "dev.profunktor" %% "redis4cats-log4cats" % `redis4cats-version`,
  )

  val fs2: List[ModuleID] = List(
    "co.fs2" %% "fs2-core" % "3.13.0",
    "co.fs2" %% "fs2-io"   % "3.13.0"
  )

  // kafka
  val `fs2-kafka-version`   = "3.9.1"
  val kafka: List[ModuleID] = List(
    "com.github.fd4s" %% "fs2-kafka" % `fs2-kafka-version`
  )

  // test
  val scalatest     = "org.scalatest" %% "scalatest"                     % "3.2.19" % Test
  val catsScalaTest = "org.typelevel" %% "cats-effect-testing-scalatest" % "1.7.0"  % Test

  val allDeps: Seq[ModuleID] = Seq(
    catsCore,
    catsEffect,
    tapirHttp4s,
    tapirSwagger,
    tapirTethys,
    tapirSttpClient,
    http4sServer,
    http4sClient,
    http4sDsl,
    sttpCore,
    sttpCats,
    logback,
    log4cats,
    logslf,
    tethysCore,
    tethysJackson,
    tethysDerivation,
    pureConfig,
    // pureConfigCore,
    telegramiumCore,
    telegramiumHigh,
    quartzScheduler,
    circuit,
    scalatest,
    catsScalaTest
  ) ++ db ++ kafka ++ fs2
}
