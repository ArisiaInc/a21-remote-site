package arisia.admin

import arisia.db.DBService
import arisia.models.LoginName

import doobie._
import doobie.implicits._

import scala.concurrent.{Future, ExecutionContext}

/**
 * Underlying services for the AdminController.
 */
trait AdminService {
  /**
   * Fetch all of the people listed as Admins for the site.
   */
  def getAdmins(): Future[List[LoginName]]

  /**
   * Add someone as an Admin.
   */
  def addAdmin(name: LoginName): Future[Int]
}

class AdminServiceImpl(
  dbService: DBService
)(
  implicit ec: ExecutionContext
) extends AdminService {

  def getAdmins(): Future[List[LoginName]] = {
    dbService.run(
      sql"""SELECT username
           |FROM permissions
           |WHERE admin = TRUE""".stripMargin
        .query[LoginName]
        .to[List]
    )
  }
  def addAdmin(name: LoginName): Future[Int] = {
    // This call, like all of these permission-sets, needs to be structured as an upsert, since we don't
    // know whether the username already exists in the table or not. Note that this upsert syntax is
    // Postgres-specific.
    dbService.run(
      sql"""INSERT INTO permissions
           |(username, admin)
           |VALUES (${name.v}, TRUE)
           |ON CONFLICT (username)
           |DO UPDATE SET admin = TRUE""".stripMargin
        .update
        .run
    )
  }
}
