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
  val betterFilesVersion = "3.9.1"

  val catsVersion = "2.1.1"

  val doobieVersion = "0.9.0"

  val macwireVersion = "2.3.7"

  val postgresDriverVersion = "42.2.18"

  val scalaTestVersion = "3.2.2"

  val scalatestPlusPlayVersion = "5.0.0"

  val swaggerUIVersion = "3.35.0"

  ///////////////////////

  val betterFiles = "com.github.pathikrit" %% "better-files" % betterFilesVersion

  val catsCore = "org.typelevel" %% "cats-core" % catsVersion

  val doobieCore = "org.tpolecat" %% "doobie-core" % doobieVersion
  val doobiePostgres = "org.tpolecat" %% "doobie-postgres" % doobieVersion

  val macwireMacros = "com.softwaremill.macwire" %% "macros" % macwireVersion % "provided"
  val macwireUtil = "com.softwaremill.macwire" %% "util" % macwireVersion

  val scalactic = "org.scalactic" %% "scalactic" % scalaTestVersion
  val scalaTest = "org.scalatest" %% "scalatest" % scalaTestVersion % "test"
  val scalatestPlusPlay = "org.scalatestplus.play" %% "scalatestplus-play" % scalatestPlusPlayVersion % Test

  val swaggerUI = "org.webjars" % "swagger-ui" % swaggerUIVersion
}
