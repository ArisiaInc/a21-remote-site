package arisia.controllers

import better.files.Resource
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
    val s = _theSchedule match {
      case Some(schedule) => schedule
      case None => {
        // EVIL: please note that the following line is *not* okay in real code, because it blocks the current
        // thread. It's legit here only because this is test code that won't run in the real site. If you
        // need to do file IO for real, let's talk about ways to do that more appropriately. (There are better
        // options, they just require a bit more code.)
        _theSchedule = Some(Resource.getAsString("konopastest.jsonp"))
        _theSchedule.get
      }
    }

    Ok(s)
  }
}
