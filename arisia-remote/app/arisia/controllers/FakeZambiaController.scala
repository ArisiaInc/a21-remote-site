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
        _theSchedule = Some(Resource.getAsString("konopastest.jsonp"))
        _theSchedule.get
      }
    }

    Ok(s)
  }
}
