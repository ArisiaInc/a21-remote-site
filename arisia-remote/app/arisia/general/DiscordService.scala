package arisia.general

import java.util.concurrent.atomic.AtomicReference

import arisia.util.Done
import play.api.libs.ws.WSClient
import play.api.libs.ws.ahc.AhcCurlRequestLogger
import play.api.{Configuration, Logging, ConfigLoader}

import scala.concurrent.{Future, ExecutionContext}

/**
 * This manages *some* of the ArisiaBot; other functions are managed via Python in Lambda.
 */
trait DiscordService {
  def getMembers(): Future[Done]
}

class DiscordServiceImpl(
  config: Configuration,
  ws: WSClient
)(
  implicit ec: ExecutionContext
) extends DiscordService with LifecycleItem with Logging {

  def botConfig[T: ConfigLoader](suffix: String) = config.get[T](s"arisia.discord.bot.$suffix")
  lazy val botEnabled = botConfig[Boolean]("enabled")
  lazy val botToken = botConfig[String]("token")
  lazy val arisiaGuildId = botConfig[String]("guildId")
  lazy val arisianRoleId = botConfig[String]("arisianRoleId")

  val lifecycleName = "DiscordService"

  def getMembers(): Future[Done] = {
    if (botEnabled) {
      ws.url(s"https://discord.com/api/guilds/$arisiaGuildId/members")
        .addHttpHeaders("Authorization" -> s"Bot $botToken")
        // TODO: what are we using this for? Do we need to loop and do pagination?
        .addQueryStringParameters("limit" -> "100")
        .withRequestFilter(AhcCurlRequestLogger())
        .get()
        .map { response =>
          println(s"Response from getMembers:\n$response\n${response.body}")
          Done
        }
    } else {
      Future.successful(Done)
    }
  }

}
