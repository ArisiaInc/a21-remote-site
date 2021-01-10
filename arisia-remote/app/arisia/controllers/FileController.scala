package arisia.controllers

import akka.util.ByteString
import arisia.auth.LoginService

import scala.concurrent.ExecutionContext
import play.api.i18n.I18nSupport
import play.api.mvc.{BaseController, ControllerComponents, EssentialAction}
import play.api.data._
import play.api.data.Forms._
import play.api.libs.streams.Accumulator
import play.core.Paths
import play.mvc.Http.MultipartFormData.{FileInfo, FilePart}

class FileController(
  val controllerComponents: ControllerComponents,
  val loginService: LoginService
)(
  implicit val ec: ExecutionContext
) extends BaseController
  with AdminControllerFuncs
  with I18nSupport {

  def manageMetadata(): EssentialAction = Action { implicit request =>
    Ok(arisia.views.html.uploadMetadata())
  }

  def uploadArtshowMetadata(): EssentialAction = Action(controllerComponents.parsers.raw) { implicit request =>
    // Rather than farting around with the terribly sophisticated and terribly hard-to-use multipart machinery
    // built into Play, we're doing this as dead-simply as we can:
    val body = request.body.asBytes().map(_.utf8String)
    println(s"The body is:\n$body")
    Redirect(routes.AdminController.home())
  }
}
