name := """neolog"""
organization := "your org"
maintainer := "your email"

version := "1.4-SNAPSHOT"
lazy val scala3 = "3.3.4"
lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := scala3

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.2" % Test

libraryDependencies ++= Seq("org.playframework" %% "play-slick" % "6.1.1",
"org.playframework" %% "play-slick-evolutions" % "6.1.1",
"com.h2database" % "h2" % "2.3.232"
)

scalacOptions ++= Seq("-feature", "-Werror")
