package arisia.discord

import arisia.general.LifecycleItem
import arisia.models.LoginUser
import arisia.util.Done
import play.api.libs.json.{Format, Json}
import play.api.libs.ws.WSClient
import play.api.libs.ws.ahc.AhcCurlRequestLogger
import play.api.{Configuration, Logging, ConfigLoader}

import scala.concurrent.{Future, ExecutionContext}

/**
 * This manages *some* of the ArisiaBot; other functions are managed via Python in Lambda.
 */
trait DiscordService {
  def getMembers(): Future[Done]

  def addArisian(who: LoginUser, creds: DiscordUserCredentials): Future[Either[String, DiscordMember]]
}

case class DiscordUserCredentials(username: String, discriminator: String)
object DiscordUserCredentials {
  implicit val fmt: Format[DiscordUserCredentials] = Json.format
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

  def loadMembers(): Future[List[DiscordMember]] = {
    if (botEnabled) {
      ws.url(s"https://discord.com/api/guilds/$arisiaGuildId/members")
        .addHttpHeaders("Authorization" -> s"Bot $botToken")
        // TODO: loop and paginate!!!
        .addQueryStringParameters("limit" -> "100")
//        .withRequestFilter(AhcCurlRequestLogger())
        .get()
        .map { response =>
          Json.parse(response.body).as[List[DiscordMember]]
        }
    } else {
      Future.successful(List.empty)
    }
  }

  def getMembers(): Future[Done] = {
    loadMembers().map { members =>
      pprint.pprintln(members)
      Done
    }
  }

  def findMemberIn(credentials: DiscordUserCredentials, members: List[DiscordMember]): Option[DiscordMember] = {
    members.find { member =>
      (member.user.username == credentials.username) && (member.user.discriminator == credentials.discriminator)
    }
  }

  def addArisian(who: LoginUser, creds: DiscordUserCredentials): Future[Either[String, DiscordMember]] = {
    // TODO: cache the member list, and try the cached list first, to reduce unnecessary loads
    // TODO: check whether these credentials are already claimed, and return a message if so
    // TODO: confirm that the current user is properly registered
    loadMembers().map { members =>
      findMemberIn(creds, members) match {
        case Some(member) => {
          // TODO:
          Right(member)
        }
        case _ => Left("Please join the Arisia Discord server first, then come back and try again!")
      }
    }
  }
}
