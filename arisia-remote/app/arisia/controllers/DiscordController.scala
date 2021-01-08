package arisia.controllers

import arisia.auth.LoginService
import arisia.discord.{DiscordService, DiscordUserCredentials}
import arisia.discord.DiscordUserCredentials
import play.api.i18n.I18nSupport
import play.api.mvc.{BaseController, ControllerComponents, EssentialAction}

import scala.concurrent.ExecutionContext

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

  def addMember(): EssentialAction = Action.async(controllerComponents.parsers.json[DiscordUserCredentials]) { implicit request =>
    val creds = request.body
    // TODO
    ???
  }
}
