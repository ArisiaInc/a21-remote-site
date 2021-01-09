package arisia.controllers

import arisia.auth.LoginService
import arisia.discord.{DiscordUserCredentials, DiscordService}
import arisia.discord.DiscordUserCredentials
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{BaseController, ControllerComponents, EssentialAction}

import scala.concurrent.{Future, ExecutionContext}

class DiscordController(
  val controllerComponents: ControllerComponents,
  val loginService: LoginService,
  discordService: DiscordService
)(
  implicit val ec: ExecutionContext
) extends BaseController
  with AdminControllerFuncs
  with I18nSupport
{
  // TODO: remove this test entry point
  def test(): EssentialAction = Action.async { implicit request =>
    discordService.getMembers().map { _ =>
      Ok("See logs")
    }
  }

  def addArisian(): EssentialAction = Action.async(controllerComponents.parsers.tolerantJson) { implicit request =>
    val result = for {
      json <- Some(request.body)
      creds <- json.asOpt[DiscordUserCredentials]
      // TODO: actually do the workflow. May need to make this level more sophisticated, for error handling
    }
      yield creds

    result.map { _ =>
      val msg = "Member added"
      Future.successful(Ok(s"""{"success":"true", "message":"$msg"}"""))
    }.getOrElse(Future.successful(BadRequest(s"""{"success":"false", "message":"That isn't the right input!"}""")))
  }
}
