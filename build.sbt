ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / fork    := false

ThisBuild / scalaVersion                                  := "2.13.18"
ThisBuild / scalafixDependencies += "org.typelevel"       %% "typelevel-scalafix" % "0.5.0"
ThisBuild / scalafixDependencies += "com.github.vovapolu" %% "scaluzzi"           % "0.1.23"
ThisBuild / scalacOptions += "-Wconf:msg=Implicit resolves to enclosing value:s"
ThisBuild / scalacOptions += "-Wconf:msg=unused value of type tethys.commons.Token:silent"
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

val common = (project in file("common"))
  .settings(libraryDependencies ++= Dependencies.allDeps)

val bot = (project in file("bot"))
  .enablePlugins(
    DockerPlugin,
    JavaAppPackaging
  )
  .settings(
    libraryDependencies ++= Dependencies.allDeps,
    Compile / mainClass  := Some("sych.ad.bot.Main"),
    Docker / packageName := "link-tracker-bot",
    dockerBaseImage      := "eclipse-temurin:21",
    dockerExposedPorts   := List(8081)
  )
  .aggregate(common)
  .dependsOn(common)

val scrapper = (project in file("scrapper"))
  .enablePlugins(
    DockerPlugin,
    JavaAppPackaging
  )
  .settings(
    libraryDependencies ++= Dependencies.allDeps,
    Compile / mainClass  := Some("sych.ad.scrapper.Main"),
    Docker / packageName := "link-tracker-scrapper",
    dockerBaseImage      := "eclipse-temurin:21",
    dockerExposedPorts   := List(8080)
  )
  .aggregate(common)
  .dependsOn(common)

val ai = (project in file("ai")).enablePlugins(
  DockerPlugin,
  JavaAppPackaging
).settings(
  name := "ai-agent",
  libraryDependencies ++= Dependencies.allDeps,
  Compile / mainClass  := Some("sych.ad.ai.Main"),
  Docker / packageName := "link-tracker-ai",
  dockerBaseImage      := "eclipse-temurin:21",
  dockerExposedPorts   := List(8082)
)
  .aggregate(common)
  .dependsOn(common)
