package arisia.schedule

import arisia.models.{ProgramItem, Schedule, ProgramPerson}
import play.api.libs.json.{Json, JsSuccess, JsError}

trait ScheduleService {
  /**
   * Given a dump from Zambia, parse that into our internal structure.
   */
  def ParseKonOpas(konopasJsonp: String): Schedule
}

class ScheduleServiceImpl()
  extends ScheduleService
{
  // TODO: wrap this in some sort of validation type, so we can cope cleanly with errors:
  def ParseKonOpas(konopasJsonp: String): Schedule = {
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

    Schedule(List.empty, List.empty)
  }
}
