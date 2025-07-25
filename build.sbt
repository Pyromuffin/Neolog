name := """neolog"""
organization := "pyromuffin"
maintainer := "pyromuffin@gmail.com"

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


// Adds additional packages into Twirl
//TwirlKeys.templateImports += "pyromuffin.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "pyromuffin.binders._"
