import scala.sys.process._
import java.io.File
import Dependencies._

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
    PlayKeys.playRunHooks += baseDirectory.map(FrontendRunHook.apply).value,
    libraryDependencies += guice,
    libraryDependencies ++= Seq(
      betterFiles,
      macwireMacros,
      macwireUtil,
      scalactic,
      scalaTest,
      scalatestPlusPlay
    )
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
      val exitCode = Process("ng build --prod", feDir).!
      println("... done building.")
      // TODO: we really ought to fail the build if the exitCode is non-zero
    }
  )
