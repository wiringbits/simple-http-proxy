name := "simple-proxy"
scalaVersion := "2.12.10"
version := "0.1.0-SNAPSHOT"
organization := "net.wiringbits"

libraryDependencies ++= Seq(guice, ws)
libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api" % "1.7.25",
  "ch.qos.logback" % "logback-core" % "1.2.3",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.scalatest" %% "scalatest" % "3.0.5" % Test,
  "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.3" % Test,
  "org.mockito" %% "mockito-scala-scalatest" % "1.7.1" % Test
)

lazy val play = (project in file("."))
  .enablePlugins(PlayScala)
  .disablePlugins(PlayLayoutPlugin)

