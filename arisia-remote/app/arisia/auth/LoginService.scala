package arisia.auth

import arisia.db.DBService

import scala.concurrent.duration._
import arisia.models.{LoginUser, LoginId, BadgeNumber, Permissions, LoginName}
import cats.data.OptionT
import cats.implicits._
import doobie.free.connection.ConnectionIO
import play.api.libs.ws.WSClient
import doobie._
import doobie.implicits._
import play.api.Configuration

import scala.concurrent.{Future, ExecutionContext}

trait LoginService {
  /**
   * Given credentials, says whether they match a known login.
   */
  def login(id: String, password: String): Future[Option[LoginUser]]

  /**
   * Fetch any additional permissions that this person might have.
   */
  def getPermissions(id: LoginId): Future[Permissions]
}

class LoginServiceImpl(
  ws: WSClient,
  dbService: DBService,
  config: Configuration,
  cmService: CMService
)(
  implicit ec: ExecutionContext
) extends LoginService {


  /**
   * These two config settings are intended for local development use only -- to allow yourself to frontend access
   * or admin access, add your CM username to these fields in your secrets.conf:
   */
  lazy val hardcodedEarlyAccess: Seq[LoginId] =
    config.get[Seq[String]]("arisia.allow.logins").map(LoginId(_))

  lazy val hardcodedAdmin: Seq[LoginId] =
    config.get[Seq[String]]("arisia.dev.admins").map(LoginId(_))

  // We use OptionT to squish together Option and Future without making ourselves crazy:
  type OptFutUser = OptionT[Future, LoginUser]

  private def checkPermissions(initialUser: LoginUser): OptFutUser = {
    val result: Future[Option[LoginUser]] = getPermissions(initialUser.id).map { perms =>
      if (perms.tech)
        Some(initialUser.copy(zoomHost = true))
      else
        Some(initialUser)
    }
    OptionT(result)
  }

  def login(idFromUser: String, password: String): Future[Option[LoginUser]] = {
    // Normalize everything to lowercase:
    val id = idFromUser.toLowerCase()
    val result = for {
      (id, badgeName) <- OptionT(cmService.checkLogin(id, password))
      details <- OptionT(cmService.fetchDetails(id))
      initialUser = LoginUser(id, badgeName, details.badgeNumber, false, details.membershipType)
      withPermissions <- checkPermissions(initialUser)
    }
      yield withPermissions

    result.value
  }

  def fetchPermissionsQuery(id: LoginId): ConnectionIO[Option[Permissions]] =
    sql"""SELECT super_admin, admin, early_access, tech
         |FROM permissions
         |WHERE username = ${id.v}""".stripMargin
    .query[Permissions]
    .option

  def getPermissions(id: LoginId): Future[Permissions] = {
    dbService.run(fetchPermissionsQuery(id)).map { dbPerms =>
      // If we didn't find an entry in the database for this LoginUser, then they have empty Permissions:
      val perms = dbPerms.getOrElse(Permissions.empty)
      val withHardcodedEarlyAccess =
        if (hardcodedEarlyAccess.contains(id)) {
          perms.copy(earlyAccess = true)
        } else {
          perms
        }
      val withHardcodedAdmin =
        if (hardcodedAdmin.contains(id)) {
          withHardcodedEarlyAccess.copy(admin = true)
        } else {
          withHardcodedEarlyAccess
        }
      withHardcodedAdmin
    }
  }
}
