package arisia.models

import play.api.libs.json._

/**
 * The representation of the entire schedule.
 */
case class Schedule(program: List[ProgramItem], people: List[ProgramPerson])

object Schedule {
  implicit val scheduleFormat: Format[Schedule] = Json.format
}
