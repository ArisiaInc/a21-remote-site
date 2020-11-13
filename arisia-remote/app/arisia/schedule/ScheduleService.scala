package arisia.schedule

import arisia.models.{ProgramItem, Schedule, ProgramPerson}
import better.files.Resource
import play.api.libs.json.{Json, JsError, JsSuccess}

import scala.concurrent.Future

trait ScheduleService {
  /**
   * Returns the currently-cached Schedule.
   */
  def currentSchedule(): Future[Schedule]

  /**
   * Given a dump from Zambia, parse that into our internal structure.
   */
  def parseKonOpas(konopasJsonp: String): Schedule
}

class ScheduleServiceImpl()
  extends ScheduleService
{
  var _theSchedule: Option[Schedule] = None

  private def fetchSchedule(): Future[Schedule] = {
    // TODO: replace this with something real, of course
    val jsonp: String = Resource.getAsString("konopastest.jsonp")
    _theSchedule = Some(parseKonOpas(jsonp))
    Future.successful(_theSchedule.get)
  }

  def currentSchedule(): Future[Schedule] = {
    _theSchedule match {
      case Some(schedule) => Future.successful(schedule)
      case None => fetchSchedule()
    }
  }

  // TODO: wrap this in some sort of validation type, so we can cope cleanly with errors:
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
