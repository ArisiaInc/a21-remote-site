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
    LoginController.loggedInUserJson() match {
      case Some(user) => {
        val result = for {
          json <- Some(request.body)
          creds <- json.asOpt[DiscordUserCredentials]
        }
          yield {
            discordService.addArisian(user, creds).map {
              _ match {
                case Left(msg) => BadRequest(s"""{"success":"false", "message":"$msg"}""")
                case Right(member) => Ok(s"""{"success":"true", "message":"You should now have access to the Arisia Discord -- welcome!"}""")
              }
            }
          }

        result.getOrElse(Future.successful(BadRequest(s"""{"success":"false", "message":"That isn't the right input!"}""")))
      }
      case _ => Future.successful(Forbidden("You need to be logged in to do this!"))
    }
  }
}
