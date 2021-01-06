package arisia.controllers

import play.api.mvc.{Request, ControllerComponents, AnyContent, BaseController, EssentialAction, Result}
import arisia.auth.LoginService
import arisia.general.SettingsService
import arisia.models.LoginUser
import play.api.Logging
import play.api.libs.json.Json

import scala.concurrent.{Future, ExecutionContext}

class SettingsController(
  val controllerComponents: ControllerComponents,
  val loginService: LoginService,
  settingsService: SettingsService
)(
  implicit val ec: ExecutionContext
) extends BaseController with UserFuncs with Logging {

  def getSettings(): EssentialAction = withLoggedInUser { userRequest =>
    settingsService.getSettings(userRequest.user).map { settings =>
      Ok(Json.toJson(settings).toString())
    }
  }

  def addSettings(): EssentialAction = withLoggedInUser { userRequest =>
    val jsonStr = userRequest.request.body.toString
    val json = Json.parse(jsonStr)
    val settings = json.as[Map[String, String]]
    settingsService.addSettings(userRequest.user, settings).map { _ =>
      NoContent
    }
  }

  def dropSetting(k: String): EssentialAction = withLoggedInUser { userRequest =>
    settingsService.dropSetting(userRequest.user, k).map { _ =>
      NoContent
    }
  }
}
