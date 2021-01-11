package arisia.auth

import doobie._
import doobie.implicits._
import cats._
import cats.effect._
import cats.implicits._
import arisia.models.{LoginName, LoginId, BadgeNumber}
import play.api.{Configuration, Logging}
import play.api.libs.ws.WSClient

import scala.concurrent.{Future, ExecutionContext}
import scala.concurrent.duration._
import scala.util.{Success, Failure}

/**
 * This is the info we get back from Convention Master.
 */
case class CMDetails(badgeNumber: BadgeNumber, active: Boolean, membershipType: MembershipType, signedCoC: Boolean)

/**
 * Service for contacting Convention Master.
 */
trait CMService {
  /**
   * Confirms that this id/password combination is a known user in CM; if so, returns the initial info about them.
   */
  def checkLogin(username: String, password: String): Future[Either[LoginError, (LoginId, LoginName)]]

  /**
   * Go out to CM directly, and fetch the details.
   */
  def fetchDetails(username: LoginId): Future[Either[LoginError, CMDetails]]
}

class CMServiceImpl(
  ws: WSClient,
  config: Configuration
)(
  implicit ec: ExecutionContext
) extends CMService with Logging {

  val badgeNamePrefix = """<input type=text name="registrant_fan_name_new" value=""""

  lazy val cmKioskUrl = config.get[String]("arisia.cm.kiosk.url")
  lazy val cmEnabled = config.get[Boolean]("arisia.cm.enabled")
  lazy val cmDriver = config.get[String]("arisia.cm.driver")
  lazy val cmUrl = config.get[String]("arisia.cm.url")
  lazy val cmUsername = config.get[String]("arisia.cm.username")
  lazy val cmPassword = config.get[String]("arisia.cm.password")

  // Test pseudo-users for development:
  lazy val cmNoAccount = config.get[String]("arisia.cm.test.user.noAccount")
  lazy val cmNotRegistered = config.get[String]("arisia.cm.test.user.notRegistered")
  lazy val cmNoCoC = config.get[String]("arisia.cm.test.user.noCoC")
  lazy val testUsers = List(cmNoAccount, cmNotRegistered, cmNoCoC)

  // The badge number to use in development, where CM integration is not enabled:
  lazy val cmTestBadgeNumber = BadgeNumber(config.get[String]("arisia.cm.test.user.badgeNumber"))

  lazy val arisiaEventId = config.get[Int]("arisia.cm.event.id")
  lazy val codeOfConductId = config.get[Int]("arisia.cm.CoC.id")

  /**
   * Check the passed-in login credentials.
   *
   * Since Convention Master (CM) owns the concept of "this is a registered member", we're going to leverage that
   * for the Remote-site login. We're doing a very simple web-scrape here: just pass the given credentials directly
   * to CM's login page, and see if we get a success or error back. Conveniently for us, the next page includes the
   * member's badge name, so we pull that out at the same time.
   */
  def checkLogin(username: String, password: String): Future[Either[LoginError, (LoginId, LoginName)]] = {
    if (!cmEnabled && testUsers.contains(username)) {
      // This is a test user, not actually in CM, so just pass it through here. Note that this code path is
      // ignored if CM integration is actually enabled
      Future.successful(Right(LoginId(username), LoginName(username)))
    } else {
      // Normal case. Note that, even if CM integration is not enabled, we still do the screen-scrape, since
      // that doesn't require any special setup:
      ws.url(cmKioskUrl)
        // In case it gets antsy about XSS. Yes, we're lying to it, but it's our site, so it really isn't an issue:
        .addHttpHeaders("origin" -> "https://reg.arisia.org")
        // TODO: propagate the error more gracefully if this times out:
        .withRequestTimeout(10.seconds)
        .post(Map(
          "regUsername" -> Seq(username),
          "mode" -> Seq("loginUser"),
          "regPassword" -> Seq(password)
        )).map { response =>
        val body = response.body
        if (body.contains("Bad username or password")) {
          // CM says that it's wrong:
          Left(LoginError.NoLogin)
        } else if (body.contains("Please Double-check your name information")) {
          // That indicates that CM thinks it's a legit username/password
          // Parse out the badgename. We're not even going to try and be cute about this
          // (parsing HTML is infamously dangerous) -- we're just going to count on the
          // precise format of the returned page:
          val badgeName = {
            val prefixPos = body.indexOf(badgeNamePrefix)
            if (prefixPos == -1) {
              username
            } else {
              val namePos = prefixPos + badgeNamePrefix.length
              val endPos = body.indexOf('"', namePos)
              val name = body.substring(namePos, endPos)
              if (name.length == 0)
                username
              else
                name
            }
          }

          Right((LoginId(username.toLowerCase), LoginName(badgeName)))
        } else {
          logger.error(s"Got unexpected response from Convention Master when checking $username!")
          Left{LoginError.NoLogin}
        }
      }
    }
  }

  //////////////////////////
  //
  // CM Database Access
  //
  // For most of the info, we need to actually go out to Convention Master and query the database directly.
  // We do this via an SSH tunnel, directly from the Remote Site to the Registration one.
  //
  // To that end, we have a local version of the DBService, tuned for this MySQL database instead of the
  // main convention Postgres one.
  //

  implicit val nonBlockingCS = IO.contextShift(ec)

  val xa = Transactor.fromDriverManager[IO](
    cmDriver,
    cmUrl,
    cmUsername,
    cmPassword
  )

  def run[T](op: ConnectionIO[T]): Future[T] = {
    val io = op.transact(xa)
    io.unsafeToFuture().andThen {
      // Log all DB errors:
      case Failure(th) => logger.error(s"Error from DB: $th")
      case Success(value) =>
    }
  }

  def fetchDetails(username: LoginId): Future[Either[LoginError, CMDetails]] = {
    if (cmEnabled) {
      fetchRealDetails(username)
    } else {
      Future.successful(fetchTestDetails(username))
    }
  }

  case class RawCMResults(
    uid: Int,
    active: Option[String],
    eventId: Option[String],
    membershipTypeStr: Option[String],
    agreementId: Option[Int],
    versionId: Option[Int]
  )

  val logHandler: LogHandler = {
    LogHandler {

      case doobie.util.log.Success(s, a, e1, e2) =>
        logger.info(s"""Successful Statement Execution:
                          |
                          |  ${s.linesIterator.dropWhile(_.trim.isEmpty).mkString("\n  ")}
                          |
                          | arguments = [${a.mkString(", ")}]
                          |   elapsed = ${e1.toMillis.toString} ms exec + ${e2.toMillis.toString} ms processing (${(e1 + e2).toMillis.toString} ms total)
          """.stripMargin)

      case doobie.util.log.ProcessingFailure(s, a, e1, e2, t) =>
        logger.error(s"""Failed Resultset Processing:
                            |
                            |  ${s.linesIterator.dropWhile(_.trim.isEmpty).mkString("\n  ")}
                            |
                            | arguments = [${a.mkString(", ")}]
                            |   elapsed = ${e1.toMillis.toString} ms exec + ${e2.toMillis.toString} ms processing (failed) (${(e1 + e2).toMillis.toString} ms total)
                            |   failure = ${t.getMessage}
          """.stripMargin)

      case doobie.util.log.ExecFailure(s, a, e1, t) =>
        logger.error(s"""Failed Statement Execution:
                            |
                            |  ${s.linesIterator.dropWhile(_.trim.isEmpty).mkString("\n  ")}
                            |
                            | arguments = [${a.mkString(", ")}]
                            |   elapsed = ${e1.toMillis.toString} ms exec (failed)
                            |   failure = ${t.getMessage}
          """.stripMargin)

    }
  }

  private def fetchRealDetails(username: LoginId): Future[Either[LoginError, CMDetails]] = {
    // We do a pretty serious join here, to fetch all of the key information in one decently reliable query
    // This *should* always return one row -- it is weird if it doesn't.
    val usernameStr = username.v
    val query =
      sql"""
        |SELECT registrant_kiosk_login.uid, account_active, events_attended.event_id, current_membership_type,
        |       registrant_agreements.agreementID, registrant_agreements.versionID
        |FROM registrant_kiosk_login
        |JOIN agreements ON agreements.agreementID=31
        |LEFT JOIN events_attended ON events_attended.uid = registrant_kiosk_login.uid
        |                          AND events_attended.event_id='32'
        |LEFT JOIN registrant_agreements ON registrant_agreements.uid=registrant_kiosk_login.uid
        |                                AND registrant_agreements.versionID=agreements.versionID
        |WHERE registrant_kiosk_login.username=$usernameStr;
        """.stripMargin
    run(
      query
        .query[RawCMResults]
        .option
    ).map { rawOpt =>
      rawOpt.map { raw =>
        CMDetails(
          // If we get any record at all, there should be a UID
          BadgeNumber(raw.uid.toString),
          // Bizarrely, this is a nullable varchar(45)
          raw.active == Some("Y"),
          // The way the query is structured, this should be NULL if they don't have a current membership
          // (Folks are welcome to check my math here: this one was hard to figure out.)
          raw.membershipTypeStr.map(MembershipType.withValue(_)).getOrElse(MembershipType.NoMembership),
          // Similarly, the Version ID of the CoC should only be set iff they have signed it.
          // (Again, checking this would be welcome.)
          raw.versionId.isDefined
        )
      }.toRight(LoginError.NoLogin)
    }
  }

  private def fetchTestDetails(username: LoginId): Either[LoginError, CMDetails] = {
    if (username.v == cmNoAccount) {
      Left(LoginError.NoLogin)
    } else if (username.v == cmNotRegistered) {
      Right(CMDetails(cmTestBadgeNumber, true, MembershipType.NoMembership, false))
    } else if (username.v == cmNoCoC) {
      Right(CMDetails(cmTestBadgeNumber, true, MembershipType.AdultStandard, false))
    } else {
      // Everyone except the test users gets through with everything clear:
      Right(CMDetails(cmTestBadgeNumber, true, MembershipType.AdultStandard, true))
    }
  }
}
