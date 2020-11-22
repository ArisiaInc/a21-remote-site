import scala.sys.process.Process

import play.sbt.PlayImport.PlayKeys

/*
 * Frontend Build hook Scripts
 *
 * TODO: This whole file is weirdly duplicative with FrontendRunHook. Refactor to do something about that,
 * if we care at all about this. (I think this is probably currently somewhat broken.)
 */

// Execution status success.
val Success = 0

// Execution status failure.
val Error = 1

// Run angular serve task when Play runs in dev mode, that is, when using 'sbt run'
// https://www.playframework.com/documentation/2.8.3/sbtCookbook
// TODO: this is a little suspicious, since it is forcing the setting instead of appending. But I get
// an undefined-setting error if this is defined as a +=
// This needs to be in the main build.sbt:
//PlayKeys.playRunHooks := Seq(baseDirectory.map(FrontendRunHook.apply).value)

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


// Create frontend build tasks for prod, dev and test execution.

lazy val `frontend-test` = TaskKey[Unit]("run frontend tests when testing application.")

`frontend-test` := {
  implicit val userInterfaceRoot = baseDirectory.value / "frontend"
  if (executeFrontendTests != Success) throw new Exception("frontend tests failed!")
}

lazy val `frontend-prod-build` = TaskKey[Unit]("run frontend build when packaging the application.")

`frontend-prod-build` := {
  implicit val userInterfaceRoot = baseDirectory.value / "frontend"
  if (executeProdBuild != Success) throw new Exception("oops! frontend build crashed.")
}

// Execute frontend prod build task prior to play dist execution.
// TODO: this doesn't compile yet! Figure it out...
//dist := (dist dependsOn `frontend-prod-build`).value

// Execute frontend prod build task prior to play stage execution.
// TODO: this doesn't compile yet! Figure it out...
//stage := (stage dependsOn `frontend-prod-build`).value

// Execute frontend test task prior to play test execution.
test := ((test in Test) dependsOn `frontend-test`).value
