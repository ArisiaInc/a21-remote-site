package arisia.controllers

import arisia.models.Schedule
import better.files.Resource
import play.api.libs.json.Json
import play.api.mvc.{BaseController, ControllerComponents, EssentialAction}

import scala.concurrent.ExecutionContext

/**
 * This is a mockup of the Zambia interface that we expect to call. We use this during development so that we
 * can inject fake schedule data to test with.
 *
 * TODO: make this much more sophisticated -- generate the schedule to be *now*, so that we are supplying the rest of the
 * system with something that can be live-tested.
 */
class FakeZambiaController (
  val controllerComponents: ControllerComponents
)(
  implicit ec: ExecutionContext
)
  extends BaseController
{
  var _theSchedule: Option[String] = None

  def getSchedule(): EssentialAction = Action { implicit request =>
    val s: String = _theSchedule match {
      case Some(schedule) => schedule
      case None => {
        // Our test data is the 2014 schedule in JSONP format. Translate that into JSON, which is what we now
        // expect.
        // EVIL: please note that the following line is *not* okay in real code, because it blocks the current
        // thread. It's legit here only because this is test code that won't run in the real site. If you
        // need to do file IO for real, let's talk about ways to do that more appropriately. (There are better
        // options, they just require a bit more code.)
        val jsonp = Resource.getAsString("konopastest.jsonp")
        val schedule = Schedule.parseKonOpas(jsonp)
        // TODO: massage the timing of the schedule so that it is happening today
        // TODO: massage the rooms in the schedule to match our actual rooms, and reduce to fewer of them
        _theSchedule = Some(Json.toJson(schedule).toString())
        _theSchedule.get
      }
    }

    Ok(s)
  }
}
