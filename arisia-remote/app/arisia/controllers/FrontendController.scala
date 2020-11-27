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
  // TODO: we're not finding index.html. What's up?
  def index(): Action[AnyContent] = assets.at("index.html")

  def assetOrDefault(resource: String): Action[AnyContent] = if (resource.startsWith(config.get[String]("apiPrefix"))){
    Action.async(r => errorHandler.onClientError(r, NOT_FOUND, "Not found"))
  } else {
    if (resource.contains(".")) assets.at(resource) else index
  }
}
