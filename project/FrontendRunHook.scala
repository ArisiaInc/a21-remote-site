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

      def makeProcessString(cmd: String): String = {
        // Windows requires npm commands prefixed with cmd /c
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
          s"cmd /c $cmd"
        } else {
          cmd
        }
      }

      /**
       * The path to the frontend directory from the Scala root directory.
       */
      val frontend: File = base / ".." / "frontend"

      /**
       * Runs the given shell command in the frontend dir, blocks until it is done, and returns the return code.
       */
      def runAndWait(cmd: String): Int = {
        val fullCmd = makeProcessString(cmd)
        Process(fullCmd, frontend).!
      }

      /**
       * Executed before play run start.
       * Run npm install if node modules are not installed.
       */
      override def beforeStarted(): Unit = {
        if (!(frontend / "node_modules").exists()) {
          println(s"Installing Angular")
          runAndWait(FrontendCommands.npmInstall)
          runAndWait(FrontendCommands.angularInstall)
          runAndWait(FrontendCommands.devkitInstall)
        }
      }

      /**
       * Executed after play run start.
       * Run npm start
       */
      override def afterStarted(): Unit = {
        println(s"Booting Angular")
        process = Some(
          Process(FrontendCommands.serve, frontend).run
        )
      }

      /**
       * Executed after play run stop.
       * Cleanup frontend execution processes.
       */
      override def afterStopped(): Unit = {
        // TODO: this isn't working! The result is that we have port 4200 blocked up, which is a problem.
        // Ammonite doesn't obviously help; nor did feeding in a ctrl-c.
        // Consider dropping down to java.lang.ProcessBuilder/Process/ProcessHandle, which collectively should
        // provide a lot more control over this.
        println(s"Destroying Process $process")
        process.foreach(_.destroy())
        process = None
      }

    }

    UIBuildHook
  }
}
