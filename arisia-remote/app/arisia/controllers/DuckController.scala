package arisia.controllers

import arisia.auth.LoginService
import arisia.fun.{Duck, DuckService}
import play.api.Logging
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{BaseController, ControllerComponents, EssentialAction}

import scala.concurrent.{Future, ExecutionContext}

class DuckController(
  val controllerComponents: ControllerComponents,
  val loginService: LoginService,
  duckService: DuckService
)(
  implicit val ec: ExecutionContext
) extends BaseController
  with AdminControllerFuncs
  with UserFuncs
  with I18nSupport
  with Logging
{
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

  def assignDuck(id: Int): EssentialAction = withLoggedInUser { userRequest =>
    // Where did this come from?
    userRequest.request.headers.get("Referer") match {
      case Some(referer) => {
        duckService.assignDuck(userRequest.user.id, id, referer).map { _ =>
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

  def dropDuck(duckId: Int): EssentialAction = withLoggedInUser { userRequest =>
    duckService.dropDuck(userRequest.user.id, duckId).map { _ =>
      NoContent
    }
  }


  ///////////////////////////////
  //
  // Duck CRUD
  //

  val duckForm = Form(
    mapping(
      "id" -> number,
      "imageUrl" -> nonEmptyText,
      "altText" -> nonEmptyText,
      "link" -> nonEmptyText,
      "hint" -> optional(text),
      "requestingUrl" -> text
    )(Duck.apply)(Duck.unapply)
  )

  def manageDucks(): EssentialAction = adminsOnly("Manage Ducks") { info =>
    implicit val request = info.request

    val ducks = duckService.getDucks()
    Ok(arisia.views.html.manageDucks(ducks))
  }

  def createDuck(): EssentialAction = adminsOnly("Show Create Duck") { info =>
    implicit val request = info.request

    Ok(arisia.views.html.editDuck(duckForm.fill(Duck.empty)))
  }
  def showEditDuck(id: Int): EssentialAction = adminsOnly("Show Edit Duck") { info =>
    implicit val request = info.request

    val ducks = duckService.getDucks()
    ducks.find(_.id == id) match {
      case Some(duck) => Ok(arisia.views.html.editDuck(duckForm.fill(duck)))
      case _ => BadRequest(s"$id isn't a known Duck!")
    }
  }

  def duckModified(): EssentialAction = adminsOnlyAsync("Duck Modified") { info =>
    implicit val request = info.request

    duckForm.bindFromRequest().fold(
      formWithErrors => {
        // TODO: actually display the error!
        Future.successful(BadRequest(arisia.views.html.editDuck(formWithErrors)))
      },
      duck => {
        info.audit(s"Duck ${duck.id}")
        val fut =
          if (duck.id == 0) {
            duckService.addDuck(duck)
          } else {
            duckService.editDuck(duck)
          }

        fut.map { _ =>
          Redirect(arisia.controllers.routes.DuckController.manageDucks())
        }
      }
    )
  }

  def removeDuck(id: Int): EssentialAction = adminsOnlyAsync(s"Remove Duck $id") { info =>
    duckService.removeDuck(id).map { _ =>
      Redirect(arisia.controllers.routes.DuckController.manageDucks())
    }
  }

}
