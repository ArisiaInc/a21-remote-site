package arisia.controllers

import java.time.{LocalDate, LocalTime}

import arisia.auth.LoginService
import arisia.models.{ProgramItemPersonName, ProgramItemTime, ProgramItemTag, ProgramItemDesc, ProgramItemDate, ProgramItem, ProgramItemPerson, ProgramItemId, ProgramItemLoc, ProgramItemTitle, ProgramPersonId}
import arisia.schedule.ScheduleService
import play.api.Logging
import play.api.mvc.{BaseController, ControllerComponents, EssentialAction}
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.I18nSupport

import scala.concurrent.{Future, ExecutionContext}

case class ScheduleInput(id: String, title: String, tags: String, time: String, mins: Int, loc: String, people: String, desc: String)
object ScheduleInput {
  val empty = ScheduleInput("", "", "", "", 60, "", "", "")
}
class ScheduleTestController(
  val controllerComponents: ControllerComponents,
  val loginService: LoginService,
  scheduleService: ScheduleService
)(
  implicit val ec: ExecutionContext
) extends BaseController
  with AdminControllerFuncs
  with I18nSupport
  with Logging
{

  val scheduleForm = Form(
    mapping(
      "id" -> text,
      "title" -> text,
      // Comma-delimited tags
      "tags" -> text,
      // For now, we are assuming that date is today
      "time" -> text,
      "mins" -> number,
      "loc" -> text,
      // Comma-delimited person IDs
      "people" -> text,
      "desc" -> text
    )(ScheduleInput.apply)(ScheduleInput.unapply)
  )

  def showTestScheduleInput(): EssentialAction = superAdminsOnly("Show Test Schedule UI") { info =>
    implicit val request = info.request

    Ok(arisia.views.html.addTestScheduleItem(scheduleForm.fill(ScheduleInput.empty)))
  }

  def addTestScheduleItem(): EssentialAction = superAdminsOnly(s"Add Test Schedule Item") { info =>
    implicit val request = info.request

    def toField[T](str: String, f: String => T): Option[T] = {
      if (str.isEmpty)
        None
      else
        Some(f(str))
    }

    scheduleForm.bindFromRequest().fold(
      formWithErrors => BadRequest(""),
      input => {
        info.audit(s"${input.title} at ${input.time}")
        val tags: List[ProgramItemTag] = input.tags.split(',').toList.map(ProgramItemTag(_))
        val time: Option[ProgramItemTime] =
          if (input.time.isEmpty)
            None
          else
            Some(ProgramItemTime(LocalTime.parse(input.time)))
        val people: List[ProgramItemPerson] =
          input.people.split(',').toList.map { id =>
            ProgramItemPerson(
              ProgramPersonId(id),
              ProgramItemPersonName(s"Participant $id")
            )
          }

        val item = ProgramItem(
          ProgramItemId(input.id),
          toField(input.title, ProgramItemTitle(_)),
          tags,
          Some(ProgramItemDate(LocalDate.now())),
          time,
          Some(input.mins.toString),
          List(ProgramItemLoc(input.loc)),
          people,
          toField(input.desc, ProgramItemDesc(_)),
          None, None, None, None, None, None
        )

        scheduleService.addTestItem(item)

        Redirect(arisia.controllers.routes.ScheduleTestController.showTestScheduleInput())
      }
    )
  }
}
