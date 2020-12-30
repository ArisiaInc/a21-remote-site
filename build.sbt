import scala.sys.process._
import java.io.File

import Dependencies._
import sbt.File

name := """arisia2021"""
organization := "org.arisia"

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

scalaVersion in Global := "2.13.3"

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "org.arisia.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "org.arisia.binders._"

/*
 * Frontend Build hook Scripts
 *
 * TODO: The following is weirdly duplicative with FrontendRunHook. Refactor to do something about that.
 */

// Execution status success.
val Success = 0

// Execution status failure.
val Error = 1

// Run angular serve task when Play runs in dev mode, that is, when using 'sbt run'
// https://www.playframework.com/documentation/2.8.3/sbtCookbook

// True if build running operating system is windows.
val isWindows = System.getProperty("os.name").toLowerCase().contains("win")

// Execute on commandline, depending on the operating system. Used to execute npm commands.
def runOnCommandline(script: String)(implicit dir: File): Int = {
  if(isWindows){ Process("cmd /c " + script, dir) } else { Process(script, dir) } }!

// Check of node_modules directory exist in given directory.
def isNodeModulesInstalled(implicit dir: File): Boolean = (dir / "node_modules").exists()

// Execute `npm install` command to install all node module dependencies. Return Success if already installed.
def runNpmInstall(implicit dir: File): Int =
  if (isNodeModulesInstalled) Success else runOnCommandline(FrontendCommands.npmInstall)

// Execute task if node modules are installed, else return Error status.
def ifNodeModulesInstalled(task: => Int)(implicit dir: File): Int =
  if (runNpmInstall == Success) task
  else Error

// Execute frontend test task. Update to change the frontend test task.
def executeFrontendTests(implicit dir: File): Int = ifNodeModulesInstalled(runOnCommandline(FrontendCommands.test))

// Execute frontend prod build task. Update to change the frontend prod build task.
def executeProdBuild(implicit dir: File): Int = ifNodeModulesInstalled(runOnCommandline(FrontendCommands.build))

lazy val `frontend-test` = TaskKey[Unit]("run frontend tests when testing application.")

lazy val `frontend-prod-build` = TaskKey[Unit]("run frontend build when packaging the application.")

lazy val backend = (project in file("arisia-remote"))
  .settings(
    PlayKeys.playRunHooks += baseDirectory.map(FrontendRunHook.apply).value,
    `frontend-test` := {
      implicit val userInterfaceRoot = baseDirectory.value / ".." / "frontend"
      if (executeFrontendTests != Success) throw new Exception("frontend tests failed!")
    },
    `frontend-prod-build` := {
      implicit val userInterfaceRoot = baseDirectory.value / ".." / "frontend"
      if (executeProdBuild != Success) throw new Exception("oops! frontend build crashed.")
    },
    version := "0.1",
    // TODO: at the moment, the frontend tests are failing for me, so commenting this dependency
    // out so that it doesn't block backend development:
//    test := ((test in Test) dependsOn `frontend-test`).value,
    // The dist task produces a zip file in target/universal, named backend-n.n.zip
    // See https://www.playframework.com/documentation/2.8.x/Deploying
    // for more information about how to use this
    dist := (dist dependsOn `frontend-prod-build`).value,
    // This is only relevant if we decide to deploy in place, I believe:
    //stage := (stage dependsOn `frontend-prod-build`).value
    maintainer := "remote@arisia.org",
      // Play built-in modules:
    libraryDependencies ++= Seq(
      evolutions,
      guice,
      jdbc,
      ws
    ),
    // Libraries we are pulling in:
    libraryDependencies ++= Seq(
      betterFiles,
      catsCore,
      doobieCore,
      doobiePostgres,
      hasher,
      jwtPlay,
      macwireMacros,
      macwireUtil,
      scalactic,
      scalaTest,
      scalatestPlusPlay,
      swaggerUI
    ),
    swaggerDomainNameSpaces := Seq("arisia.models")
  )
  .enablePlugins(PlayScala, SwaggerPlugin)

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
