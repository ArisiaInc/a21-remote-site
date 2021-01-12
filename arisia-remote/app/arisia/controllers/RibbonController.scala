package arisia.controllers

import arisia.auth.LoginService
import arisia.fun.{RibbonService, Ribbon}
import arisia.util._
import play.api.Logging
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{BaseController, ControllerComponents, EssentialAction}

import scala.concurrent.{Future, ExecutionContext}

class RibbonController(
  val controllerComponents: ControllerComponents,
  val loginService: LoginService,
  ribbonService: RibbonService
)(
  implicit val ec: ExecutionContext
) extends BaseController
  with AdminControllerFuncs
  with UserFuncs
  with I18nSupport
  with Logging
{

  def getRibbons(): EssentialAction = withLoggedInUser { userRequest =>
    val headers = userRequest.request.headers

    if (headers.get("secret").isDefined) {
      val secret = headers("secret")
      ribbonService.getRibbon(secret) match {
        case Some(ribbon) => fut(Ok(Json.toJson(ribbon).toString))
        case _ => fut(NotFound(""))
      }
    } else if (headers.get("selfServe").isDefined) {
      fut(Ok(Json.toJson(ribbonService.getSelfServeRibbons())))
    } else {
      // All of the ribbons for this user
      ribbonService.getRibbonsFor(userRequest.user.id).map ( results =>
        Ok(Json.toJson(results))
      )
    }
  }

  def assignRibbon(id: Int): EssentialAction = Action(controllerComponents.parsers.tolerantJson).async { implicit request =>
    LoginController.loggedInUserJson() match {
      case Some(user) => {
        val json = request.body
        val secret = (json \ "secret").as[String]
        ribbonService.assignRibbon(user.id, id, secret).map { _ =>
          NoContent
        }
      }
      case _ => Future.successful(Forbidden(s"""{"success":false, "message":"You're not logged in! Please log in and try again."}"""))
    }
  }

  def arrangeRibbons(): EssentialAction =  Action(controllerComponents.parsers.tolerantJson).async { implicit request =>
    LoginController.loggedInUserJson() match {
      case Some(user) => {
        val json = request.body
        val order = (json \ "ribbonIds").as[List[Int]]
        ribbonService.orderRibbons(user.id, order).map { _ =>
          NoContent
        }
      }
      case _ => Future.successful(Forbidden(s"""{"success":false, "message":"You're not logged in! Please log in and try again."}"""))
    }
  }

  ///////////////////////////////
  //
  // Ribbon CRUD
  //

  val ribbonForm = Form(
    mapping(
      "id" -> number,
      "ribbonText" -> nonEmptyText,
      "colorFg" -> optional(text),
      "colorBg" -> optional(text),
      "gradient" -> optional(text),
      "imageFg" -> optional(text),
      "imageBg" -> optional(text),
      "secret" -> text,
      "selfService" -> boolean
    )(Ribbon.apply)(Ribbon.unapply)
  )

  def manageRibbons(): EssentialAction = adminsOnly("Manage Ribbons") { info =>
    implicit val request = info.request

    val ribbons = ribbonService.getAllRibbons()
    Ok(arisia.views.html.manageRibbons(ribbons))
  }

  def createRibbon(): EssentialAction = adminsOnly("Show Create Ribbon") { info =>
    implicit val request = info.request

    Ok(arisia.views.html.editRibbon(ribbonForm.fill(Ribbon.empty)))
  }
  def showEditRibbon(id: Int): EssentialAction = adminsOnly("Show Edit Ribbon") { info =>
    implicit val request = info.request

    ribbonService.getRibbon(id) match {
      case Some(ribbon) => Ok(arisia.views.html.editRibbon(ribbonForm.fill(ribbon)))
      case _ => BadRequest(s"$id isn't a known Ribbon!")
    }
  }

  def ribbonModified(): EssentialAction = adminsOnlyAsync("Ribbon Modified") { info =>
    implicit val request = info.request

    ribbonForm.bindFromRequest().fold(
      formWithErrors => {
        // TODO: actually display the error!
        Future.successful(BadRequest(arisia.views.html.editRibbon(formWithErrors)))
      },
      ribbon => {
        info.audit(s"Ribbon ${ribbon.id}")
        val fut =
          if (ribbon.id == 0) {
            ribbonService.addRibbon(ribbon)
          } else {
            ribbonService.editRibbon(ribbon)
          }

        fut.map { _ =>
          Redirect(arisia.controllers.routes.RibbonController.manageRibbons())
        }
      }
    )
  }

  def removeRibbon(id: Int): EssentialAction = adminsOnlyAsync(s"Remove Ribbon $id") { info =>
    ribbonService.removeRibbon(id).map { _ =>
      Redirect(arisia.controllers.routes.RibbonController.manageRibbons())
    }
  }

}
