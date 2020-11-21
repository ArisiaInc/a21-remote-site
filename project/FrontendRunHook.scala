import java.net.InetSocketAddress

import play.sbt.PlayRunHook
import sbt._

import scala.sys.process.Process

/**
 * Frontend build play run hook.
 * @link https://www.playframework.com/documentation/2.8.3/sbtCookbook
 * When Play runs in dev mode (sbt run) we can start up additional processes
 * that are required for development.
 * This can be done by defining a PlayRunHook, which is a trait with the following methods:
 * - beforeStarted(): Unit - called before the play application is started, but after all “before run”
 *      tasks have been completed.
 * - afterStarted(): Unit - called after the play application has been started.
 * - afterStopped(): Unit - called after the play process has been stopped.
 */
object FrontendRunHook {
  def apply(base: File): PlayRunHook = {
    object UIBuildHook extends PlayRunHook {

      var process: Option[Process] = None

      /**
       * Change these commands if you want to use Yarn.
       */
      var npmInstall: String = FrontendCommands.dependencyInstall
      var npmRun: String = FrontendCommands.serve

      // Windows requires npm commands prefixed with cmd /c
      if (System.getProperty("os.name").toLowerCase().contains("win")){
        npmInstall = "cmd /c" + npmInstall
        npmRun = "cmd /c" + npmRun
      }

      /**
       * Executed before play run start.
       * Run npm install if node modules are not installed.
       */
      override def beforeStarted(): Unit = {
        if (!(base / "frontend" / "node_modules").exists()) Process(npmInstall, base / "frontend").!
      }

      /**
       * Executed after play run start.
       * Run npm start
       */
      override def afterStarted(): Unit = {
        process = Option(
          Process(npmRun, base / "frontend").run
        )
      }

      /**
       * Executed after play run stop.
       * Cleanup frontend execution processes.
       */
      override def afterStopped(): Unit = {
        process.foreach(_.destroy())
        process = None
      }

    }

    UIBuildHook
  }
}
