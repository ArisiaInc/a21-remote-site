import scala.sys.process._
import java.io.File

name := """arisia2021"""
organization := "org.arisia"

version := "1.0-SNAPSHOT"

val build = taskKey[Unit]("Build everything")
val buildFrontend = taskKey[Unit]("Build the Angular front end")

lazy val root = (project in file("."))
    .aggregate(backend, frontend)
    .settings(
      build := {
        (backend / Compile / compile).value
        (frontend / buildFrontend).value
      }
    )

scalaVersion := "2.13.3"

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "org.arisia.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "org.arisia.binders._"

lazy val backend = (project in file("arisia-remote"))
  .settings(
    libraryDependencies += guice,
    libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
  )
  .enablePlugins(PlayScala)

lazy val frontend = (project in file("frontend"))
  .settings(
    buildFrontend := {
      println("Building the frontend...")
      // Technically, we should probably make this a Setting, but whatever:
      val feDir: File = new File("./frontend")
      // This build process outputs an enormous number of blank lines for some reason, which is confusing.
      // Can we suppress those?
      val exitCode = Process("ng build", feDir).!
      println("... done building.")
      // TODO: we really ought to fail the build if the exitCode is non-zero
    }
  )