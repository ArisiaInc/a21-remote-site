package arisia.controllers

import controllers.Assets
import play.api.Configuration
import play.api.http._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class FrontendController(
  val controllerComponents: ControllerComponents,
  assets: Assets,
  errorHandler: HttpErrorHandler,
  config: Configuration
)(
  implicit ec: ExecutionContext
)
  extends BaseController
  with UserFuncs
{
  def index(): Action[AnyContent] = assets.at("/frontend/index.html")

  def assetOrDefault(resource: String): Action[AnyContent] =
    if (resource.contains(".")) assets.versioned(s"/frontend/$resource") else index()

  def getConfigEntry(name: String): EssentialAction = withLoggedInUser { userRequest =>
    config.getOptional[String](s"arisia.frontend.$name") match {
      case Some(v) => Future.successful(Ok(v))
      case _ => Future.successful(NotFound(""))
    }
  }
}
