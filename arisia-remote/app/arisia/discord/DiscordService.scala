package arisia.discord

import java.util.concurrent.atomic.AtomicReference

import arisia.auth.LoginService
import arisia.general.LifecycleItem
import arisia.models.{LoginUser, BadgeNumber}
import arisia.util.Done
import play.api.libs.json.{Format, JsObject, JsArray, JsString, Json}
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

  def generateAssistSecret(who: LoginUser): String

  def addArisianAssisted(creds: DiscordHelpCredentials): Future[Either[String, DiscordMember]]
}

case class DiscordUserCredentials(username: String, discriminator: String)
object DiscordUserCredentials {
  implicit val fmt: Format[DiscordUserCredentials] = Json.format
}

case class DiscordHelpCredentials(badgeNumber: String, secret: String, discordId: String)
object DiscordHelpCredentials {
  implicit val fmt: Format[DiscordHelpCredentials] = Json.format
}

class DiscordServiceImpl(
  config: Configuration,
  ws: WSClient,
  loginService: LoginService
)(
  implicit ec: ExecutionContext
) extends DiscordService with LifecycleItem with Logging {

  def botConfig[T: ConfigLoader](suffix: String) = config.get[T](s"arisia.discord.bot.$suffix")
  lazy val botEnabled = botConfig[Boolean]("enabled")
  lazy val baseUrl = botConfig[String]("baseUrl")
  lazy val botToken = botConfig[String]("token")
  lazy val arisiaGuildId = botConfig[String]("guildId")
  lazy val arisianRoleId = botConfig[String]("arisianRoleId")
  lazy val pageSize = botConfig[Int]("page.size").toString

  val lifecycleName = "DiscordService"

  val memberCache: AtomicReference[List[DiscordMember]] = new AtomicReference(List.empty)

  def loadMembers(): Future[List[DiscordMember]] = {
    // TODO: we should maybe move these botEnabled checks out, and instead return a dummy service if it is *not*
    // enabled
    if (botEnabled) {
      logger.info("Loading Arisia server members from Discord")
      def loadRecursive(startingAt: String): Future[List[DiscordMember]] = {
        ws.url(s"$baseUrl/guilds/$arisiaGuildId/members")
          .addHttpHeaders("Authorization" -> s"Bot $botToken")
          .addQueryStringParameters(
            "limit" -> pageSize,
            "after" -> startingAt
          )
          //        .withRequestFilter(AhcCurlRequestLogger())
          .get()
          .flatMap { response =>
            // Note that the response is in ascending order by User ID; we reverse that so that we can keep
            // tacking more members onto the front, to preverse decent O(n) behavior as we loop:
            Json.parse(response.body).asOpt[List[DiscordMember]] match {
              case Some(membersAsc) => {
                val members = membersAsc.reverse
                if (members.isEmpty) {
                  Future.successful(members)
                } else {
                  // Here's where we recurse, to fetch the next block
                  // Note that members is in ascending order by user ID; that's the
                  // page indicator:
                  loadRecursive(members.head.user.id).map {
                    _ ++ members
                  }
                }
              }
              case _ => {
                logger.error(s"Got unexpected response from Discord getMember:\n${response.body}")
                Future.successful(List.empty)
              }
            }
          }
      }

      loadRecursive("0").map { members =>
        memberCache.set(members)
        members
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

  def findMember(credentials: DiscordUserCredentials): Future[Option[DiscordMember]] = {
    findMemberIn(credentials, memberCache.get()) match {
      case Some(member) => {
        // We already had them loaded, so we're set:
        Future.successful(Some(member))
      }
      case _ => {
        loadMembers().map { members =>
          findMemberIn(credentials, members)
        }
      }
    }
  }

  def setDiscordRoles(member: DiscordMember): Future[Done] = {
    if (botEnabled) {
      ws.url(s"$baseUrl/guilds/$arisiaGuildId/members/${member.user.id}/roles/$arisianRoleId")
        .addHttpHeaders(
          "Authorization" -> s"Bot $botToken"
        )
//        .withRequestFilter(AhcCurlRequestLogger())
        .put(JsObject.empty)
        .map { response =>
          if (response.status == 204) {
            // The expected case
            Done
          } else {
            logger.error(
              s"Discord failure when trying to make ${member.user.username}#${member.user.discriminator} an Arisian:\n${response.body}")
            Done
          }
        }
    } else {
      Future.successful(Done)
    }
  }

  def setBadgeName(who: LoginUser, member: DiscordMember): Future[Done] = {
    if (botEnabled) {
      ws.url(s"$baseUrl/guilds/$arisiaGuildId/members/${member.user.id}")
        .addHttpHeaders(
          "Authorization" -> s"Bot $botToken",
          "Content-Type" -> "application/json"
        )
        .patch(Json.obj(
          "nick" -> who.name.v
        ))
        .map { response =>
          if (response.status == 204) {
            // The expected case
            Done
          } else {
            logger.error(
              s"Discord failure when trying to set ${member.user.username}#${member.user.discriminator} nick to ${who.name.v}:\n${response.body}")
            Done
          }
        }
    } else {
      Future.successful(Done)
    }
  }

  def addArisianCore(who: LoginUser, member: DiscordMember): Future[Either[String, DiscordMember]] = {
    for {
      _ <- setDiscordRoles(member)
      _ <- setBadgeName(who, member)
      _ <- loginService.addDiscordInfo(who, member)
    }
      yield Right(member)
  }

  def addArisian(who: LoginUser, creds: DiscordUserCredentials): Future[Either[String, DiscordMember]] = {
    // TODO: check whether this discord user ID (not username#discriminator) is already claimed, and return a message if so
    // TODO: If it is claimed by *this* user, then update it is success -- should we resync?
    findMember(creds).flatMap {
      _  match {
        case Some(member) => {
          addArisianCore(who, member)
        }
        case _ =>
          Future.successful(Left("Please join the Arisia Discord server first, then come back and try again!"))
      }
    }
  }

  def generateAssistSecret(who: LoginUser): String = {
    // TODO: figure out the right into for this, and sign it properly
    s"FakeSecretFor${who.badgeNumber.v}"
  }

  def validateAssistSecret(secret: String): Boolean = {
    // TODO: once we have a real secret, do this:
    true
  }

  def addArisianAssisted(creds: DiscordHelpCredentials): Future[Either[String, DiscordMember]] = {
    if (validateAssistSecret(creds.secret)) {
      // Note that, in this pathway, we don't know the
      val member = DiscordMember(DiscordUser(creds.discordId, "", ""))
      loginService.fetchUserInfo(BadgeNumber(creds.badgeNumber)).flatMap {
        _ match {
          case Some(user) => {
            addArisianCore(user, member)
          }
          case _ => Future.successful(Left("User not found!"))
        }
      }
    } else {
      Future.successful(Left("The user secret isn't valid!"))
    }
  }
}
