import sbt._

/**
 * This is where all the external Scala library dependencies are defined.
 *
 * The syntax for these definitions is sbt standard, and is basically an encoding of standard Maven coordinates:
 *
 *   "com.organization.whatever" %% "artifact" % "version"
 *
 * The %% means that the standard Scala-version suffix "_.2.13" will be appended to the artifact name.
 */
object Dependencies {
  val macwireVersion = "2.3.7"

  val scalatestPlusPlayVersion = "5.0.0"

  val macwireMacros = "com.softwaremill.macwire" %% "macros" % macwireVersion % "provided"
  val macwireUtil = "com.softwaremill.macwire" %% "util" % macwireVersion

  val scalatestPlusPlay = "org.scalatestplus.play" %% "scalatestplus-play" % scalatestPlusPlayVersion % Test
}
