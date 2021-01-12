package arisia.controllers

import controllers.Assets
import play.api.Configuration
import play.api.http._
import play.api.mvc._

import scala.concurrent.ExecutionContext

class FrontendController(
  val controllerComponents: ControllerComponents,
  assets: Assets,
  errorHandler: HttpErrorHandler,
  config: Configuration
)(
  implicit ec: ExecutionContext
)
  extends BaseController
{
  def index(): Action[AnyContent] = assets.at("/frontend/index.html")

  def assetOrDefault(resource: String): Action[AnyContent] =
    if (resource.contains(".")) assets.versioned(s"/frontend/$resource") else index()

  def getConfigEntry(name: String): EssentialAction = Action { implicit request =>
    config.getOptional[String](s"arisia.frontend.$name") match {
      case Some(v) => Ok(v)
      case _ => NotFound("")
    }
  }
}
