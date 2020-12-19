package arisia.admin

import arisia.db.DBService
import arisia.models.{LoginName, LoginId}
import doobie._
import doobie.implicits._
import doobie.util.fragment
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

  class PermissionColumn(name: String) {
    val column = Fragment.const(name)

    def getMembers(): Future[List[LoginId]] = {
      val query =
        fr"""SELECT username
          FROM permissions
          WHERE""" ++ column ++ fr"= TRUE"
      dbService.run(
        query
          .query[LoginId]
          .to[List]
      )
    }

    def addMember(id: LoginId): Future[Int] = {
      logger.info(s"Adding ${id.v} as a $name")
      // This call needs to be structured as an upsert, since we don't
      // know whether the username already exists in the table or not. Note that this upsert syntax is
      // Postgres-specific.
      val query =
        fr"""INSERT INTO permissions
           (username, """ ++ column ++ fr""")
           VALUES (${id.v}, TRUE)
           ON CONFLICT (username)
           DO UPDATE SET""" ++ column ++ fr"= TRUE"
      dbService.run(
        query
          .update
          .run
      )
    }

    def removeMember(id: LoginId): Future[Int] = {
      logger.info(s"Removing ${id.v} as a $name")
      val query =
        fr"""UPDATE permissions
           SET""" ++ column ++ fr"""= FALSE
           WHERE username = ${id.v}"""
      dbService.run(
        query
          .update
          .run
      )
    }
  }

  lazy val admins = new PermissionColumn("admin")
  def getAdmins(): Future[List[LoginId]] = admins.getMembers()
  def addAdmin(id: LoginId): Future[Int] = admins.addMember(id)
  def removeAdmin(id: LoginId): Future[Int] = admins.removeMember(id)
}
