package arisia.models

import play.api.libs.json._
import com.roundeights.hasher.Implicits._
import scala.language.postfixOps

/**
 * The representation of the entire schedule.
 */
case class Schedule(program: List[ProgramItem], people: List[ProgramPerson]) {
  lazy val json: String = Json.toJson(this).toString()
  // Note that we do *not* care about a cryptographically-sound hash here -- we just need something large and
  // well-enough-distributed to make hash collisions unlikely. So MD5 should suffice -- we don't need to waste
  // extra cycles on SHA256 or BCrypt:
  lazy val hash: String = json.md5.hex
}

object Schedule {
  implicit val scheduleFormat: Format[Schedule] = Json.format

  val empty = Schedule(List.empty, List.empty)

  /**
   * Given a dump from Zambia, parse that into our internal structure.
   *
   * TODO: this is mostly obsolete, since we are now getting JSON from Zambia instead of KonOpas JSONP.
   */
  def parseKonOpas(konopasJsonp: String): Schedule = {
    // We are assuming the format of the JSONP pretty precisely -- it's generated, so we should be able to count
    // on it. This is a known fragility, but we aren't pretending that this is general-purpose at this stage of the
    // game.
    val lines = konopasJsonp.split("\n")

    val programLine = lines(0)
    val peopleLine = lines(1)

    // For each line, drop the JSONP variable declaration at the front, and the semicolon at the end:
    val programLinePrefix = "export const program = "
    val peopleLinePrefix = "export const people = "

    val programJson = programLine.drop(programLinePrefix.length).dropRight(1)
    val peopleJson = peopleLine.drop(peopleLinePrefix.length).dropRight(1)

    val program: List[ProgramItem] =
      Json.parse(programJson).validate[List[ProgramItem]] match {
        case JsSuccess(value, path) => value
        case JsError(errors) => {
          println(s"Failed to read the Program: first error -- ${errors.head}")
          throw new Exception(s"Failure while trying to read the program")
        }
      }
    val people: List[ProgramPerson] =
      Json.parse(peopleJson).validate[List[ProgramPerson]] match {
        case JsSuccess(value, path) => value
        case JsError(errors) => {
          println(s"Failed to read the People: first error -- ${errors.head}")
          throw new Exception(s"Failure while trying to read the people")
        }
      }

    Schedule(program, people)
  }
}
