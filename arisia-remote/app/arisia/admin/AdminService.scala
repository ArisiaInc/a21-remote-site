package arisia.admin

import arisia.db.DBService
import arisia.models.{LoginName, LoginId}
import doobie._
import doobie.implicits._
import play.api.Logging

import scala.concurrent.{Future, ExecutionContext}

/**
 * Underlying services for the AdminController.
 */
trait AdminService {
  /**
   * Fetch all of the people listed as Admins for the site.
   */
  def getAdmins(): Future[List[LoginId]]

  /**
   * Add someone as an Admin.
   */
  def addAdmin(name: LoginId): Future[Int]

  /**
   * Take away this person's Admin rights.
   */
  def removeAdmin(name: LoginId): Future[Int]
}

class AdminServiceImpl(
  dbService: DBService
)(
  implicit ec: ExecutionContext
) extends AdminService with Logging {

  def getAdmins(): Future[List[LoginId]] = {
    dbService.run(
      sql"""SELECT username
           |FROM permissions
           |WHERE admin = TRUE""".stripMargin
        .query[LoginId]
        .to[List]
    )
  }

  def addAdmin(id: LoginId): Future[Int] = {
    logger.info(s"Adding ${id.v} as an Admin")
    // This call, like all of these permission-sets, needs to be structured as an upsert, since we don't
    // know whether the username already exists in the table or not. Note that this upsert syntax is
    // Postgres-specific.
    // TODO: refactor this call into Doobie Fragments, so that we can reuse the same upsert logic for all
    // of the different flags.
    dbService.run(
      sql"""INSERT INTO permissions
           |(username, admin)
           |VALUES (${id.v}, TRUE)
           |ON CONFLICT (username)
           |DO UPDATE SET admin = TRUE""".stripMargin
        .update
        .run
    )
  }

  def removeAdmin(id: LoginId): Future[Int] = {
    logger.info(s"Removing ${id.v} as an Admin")
    dbService.run(
      sql"""UPDATE permissions
           |SET admin = FALSE
           |WHERE username = ${id.v}""".stripMargin
        .update
        .run
    )
  }
}
