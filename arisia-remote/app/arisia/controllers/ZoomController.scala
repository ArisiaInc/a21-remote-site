package arisia.controllers

import arisia.zoom.ZoomService
import play.api.mvc.{BaseController, EssentialAction, ControllerComponents}

import scala.concurrent.ExecutionContext

/**
 * This controller manages all external entry points that are Zoom-specific.
 */
class ZoomController(
  val controllerComponents: ControllerComponents,
  zoomService: ZoomService
)(
  implicit ec: ExecutionContext
) extends BaseController {

  // TODO: get rid of this. There isn't much harm to it, but we won't need it once integration is really working,
  // and there is no point leaving extra entry points around for potential mischief.
  def test(): EssentialAction = Action.async { implicit request =>
    zoomService.getUsers().map { _ =>
      Ok("Got it!")
    }
  }
}
