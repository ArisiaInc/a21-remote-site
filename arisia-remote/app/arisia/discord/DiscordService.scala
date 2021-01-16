package arisia.discord

import java.util.concurrent.atomic.AtomicReference
import java.util.Base64

import arisia.auth.LoginService
import arisia.general.{LifecycleItem, LifecycleService}
import arisia.models.{BadgeNumber, LoginUser}
import arisia.timer.TimerService
import arisia.util.Done
import play.api.libs.json.{Format, JsObject, JsArray, JsString, Json}
import play.api.libs.ws.WSClient
import play.api.libs.ws.ahc.AhcCurlRequestLogger
import play.api.{Configuration, Logging, ConfigLoader}
import com.roundeights.hasher.Hasher

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Future, ExecutionContext}

/**
 * This manages *some* of the ArisiaBot; other functions are managed via Python in Lambda.
 */
trait DiscordService {
  def getMembers(): Future[Done]

  def addArisian(who: LoginUser, creds: DiscordUserCredentials): Future[Either[String, DiscordMember]]

  def generateAssistSecret(who: LoginUser): String

  def addArisianAssisted(creds: DiscordHelpCredentials): Future[Either[String, DiscordMember]]

  def syncUser(who: LoginUser, discordId: String): Future[Done]
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
  loginService: LoginService,
  timerService: TimerService,
  val lifecycleService: LifecycleService
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
  lazy val loadMembersInterval = botConfig[FiniteDuration]("load.members.interval")

  lazy val secretKey = config.get[String]("play.http.secret.key")

  val lifecycleName = "DiscordService"
  lifecycleService.register(this)
  override def init(): Future[Done] = {
    timerService.register("Load Discord Member List", loadMembersInterval) { now =>
      // Note that this is fire-and-forget
      loadMembers()
      ()
    }
    Future.successful(Done)
  }

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
    pprint.pprintln(memberCache.get())
    Future.successful(Done)
  }

  def findMemberIn(credentials: DiscordUserCredentials, members: List[DiscordMember]): Option[DiscordMember] = {
    members.find { member =>
      (member.user.username == credentials.username) && (member.user.discriminator == credentials.discriminator)
    }
  }

  def findMember(credentials: DiscordUserCredentials): Option[DiscordMember] = {
    findMemberIn(credentials, memberCache.get())
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

  def setBadgeName(who: LoginUser, memberId: String): Future[Done] = {
    if (botEnabled && !who.name.isEmpty) {
      ws.url(s"$baseUrl/guilds/$arisiaGuildId/members/${memberId}")
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
              s"Discord failure when trying to set $memberId nick to ${who.name.v}:\n${response.body}")
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
      _ <- setBadgeName(who, member.user.id)
      _ <- loginService.addDiscordInfo(who, member)
    }
      yield Right(member)
  }

  def addArisian(who: LoginUser, creds: DiscordUserCredentials): Future[Either[String, DiscordMember]] = {
    loginService.userFromCredentials(creds).flatMap {
      _ match {
        case None => {
          // The normal path: these credentials have not been claimed yet
          findMember(creds) match {
            case Some(member) => {
              addArisianCore(who, member)
            }
            case _ =>
              Future.successful(
                Left(
                  "Please join the Arisia Discord server first. If you have already done so, please wait five minutes and try this again -- it takes time for things to catch up."))
          }
        }
        case Some((userId, discordId)) => {
          // Hmm. These credentials have already been claimed. Is it the same user?
          if (userId == who.id) {
            // It's the same person
            // TODO: should we consider this a resync?
            Future.successful(Left(s"You have already connected your Discord account."))
          } else {
            val msg = s"${who.id.v} attempting to claim already-claimed Discord account $discordId, which belongs to ${userId.v}!"
            logger.warn(msg)
            Future.successful(Left(s"Those Discord credentials are already claimed."))
          }
        }
      }
    }
  }

  def generateAssistSecret(who: LoginUser): String = {
    DiscordService.generateAssistSecret(secretKey)(who)
  }

  def validateAssistSecret(secret: String): Boolean = {
    DiscordService.validateAssistSecret(secretKey)(secret)
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

  def syncUser(who: LoginUser, discordId: String): Future[Done] = {
    // TODO: in airy theory this should set Roles, but we don't have any interesting ones that change yet:
    setBadgeName(who, discordId)
  }
}

object DiscordService {
  lazy val encoder = Base64.getEncoder
  lazy val decoder = Base64.getDecoder

  // Pulled out to make this testable:
  def generateAssistSecret(secretKey: String)(who: LoginUser): String = {
    val hash = Hasher(who.badgeNumber.v).hmac(secretKey).md5
    val bytes = hash.bytes
    // Drop the "==" at the end:
    val base64 = new String(encoder.encode(bytes)).dropRight(2)
    s"${who.badgeNumber.v}:$base64"
  }

  def validateAssistSecret(secretKey: String)(secret: String): Boolean = {
    secret.split(':').toList match {
      case badgeNumber :: base64 :: Nil => {
        val bytes = (base64 + "==").getBytes
        val hashBytes = decoder.decode(bytes)
        Hasher(badgeNumber).hmac(secretKey).md5.hash = hashBytes
      }
      case _ => false
    }
  }

}
