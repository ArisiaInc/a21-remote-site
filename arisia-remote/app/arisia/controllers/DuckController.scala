package arisia.controllers

import arisia.fun.DuckService
import play.api.libs.json.Json
import play.api.mvc.{BaseController, ControllerComponents, EssentialAction}

import scala.concurrent.{ExecutionContext, Future}

class DuckController(
  val controllerComponents: ControllerComponents,
  duckService: DuckService
)(
  implicit ec: ExecutionContext
) extends BaseController {
  def getDucks(): EssentialAction = Action { implicit request =>
    val ducks = duckService.getDucks()
    Ok(Json.toJson(ducks).toString())
  }

  def getDuck(id: Int): EssentialAction = Action { implicit request =>
    duckService.getDuck(id) match {
      case Some(duck) => Ok(Json.toJson(duck).toString())
      case _ => NotFound(s"""{"success":false, "message":"There is no duck $id"}""")
    }
  }

  def assignDuck(id: Int): EssentialAction = Action.async { implicit request =>
    LoginController.loggedInUser() match {
      case Some(user) => {
        // Where did this come from?
        request.headers.get("Referer") match {
          case Some(referer) => {
            duckService.assignDuck(user.id, id, referer).map { _ =>
              Created("")
            }.recover {
              case ex: Exception => BadRequest("")
            }
          }
          case _ => {
            // No referer, so we can't validate the duck claim
            Future.successful(BadRequest(""))
          }
        }
      }
      case _ => {
        Future.successful(Forbidden(s"""{"success":false, "message":"You're not logged in, so you can't have ducks! Sorry..."}"""))
      }
    }
  }
}
