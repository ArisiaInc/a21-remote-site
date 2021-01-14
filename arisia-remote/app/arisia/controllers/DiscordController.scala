package arisia.controllers

import arisia.auth.LoginService
import arisia.discord.{DiscordHelpCredentials, DiscordUserCredentials, DiscordService}
import play.api.{Configuration, Logging}
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{BaseController, ControllerComponents, EssentialAction}

import scala.concurrent.{Future, ExecutionContext}

class DiscordController(
  val controllerComponents: ControllerComponents,
  val loginService: LoginService,
  discordService: DiscordService,
  config: Configuration
)(
  implicit val ec: ExecutionContext
) extends BaseController
  with AdminControllerFuncs
  with UserFuncs
  with I18nSupport
  with Logging
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

  val sharedSecret = config.get[String]("arisia.discord.bot.shared.secret")

  def generateAssistSecret(): EssentialAction = withLoggedInUser { userRequest =>
    Future.successful(Ok(discordService.generateAssistSecret(userRequest.user)))
  }

  def assistedAddArisian(): EssentialAction = Action(controllerComponents.parsers.tolerantJson)  { implicit request =>
    request.headers.get("X-Shared-Secret") match {
      case Some(secret) if (secret == sharedSecret) => {
        request.body.asOpt[DiscordHelpCredentials] match {
          case Some(creds) => {
            Future {
              discordService.addArisianAssisted(creds).map {
                _ match {
                  case Right(member) => {}
                  case Left(error) => logger.error(s"Got error trying to add Arisian assisted: $error")
                }
              }
            }
            Ok("")
          }
          case _ => BadRequest("Malformed request to help add an Arisian")
        }
      }
      case _ => Unauthorized("Shared secret not found in the X-Shared")
    }
  }

  def sync(id: String): EssentialAction = Action { implicit request =>
    request.headers.get("X-Shared-Secret") match {
      case Some(secret) if (secret == sharedSecret) => {
        // Lambda needs a fast response, and doesn't give a damn about the value of that response. So do the
        // standard webhook fire-and-forget:
        Future {
          loginService.fetchUserFromDiscordId(id).map {
            _ match {
              case Some(user) => {
                discordService.syncUser(user, id)
              }
              case _ => logger.warn(s"Got a sync request for unknown Discord user $id!")
            }
          }
        }
        Ok("")
      }
      case _ => Unauthorized("Shared secret not found in the X-Shared")
    }
  }
}
