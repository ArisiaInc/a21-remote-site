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
    dbService.run(
      sql"""INSERT INTO permissions
           |(username, admin)
           |VALUES (${name.v}, TRUE)""".stripMargin
        .update
        .run
    )
  }
}
