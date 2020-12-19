package arisia.admin

import arisia.db.DBService
import arisia.models.LoginId
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

  def getEarlyAccess(): Future[List[LoginId]]
  def addEarlyAccess(id: LoginId): Future[Int]
  def removeEarlyAccess(id: LoginId): Future[Int]
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

  // TODO: in a perfect world, we would do something more typeclass-based, to remove the boilerplate of all these
  // distinct calls. But I don't have time right now to work out all the interlocking classes needed to make it
  // properly strongly-typed all the way down.

  lazy val admins = new PermissionColumn("admin")
  def getAdmins(): Future[List[LoginId]] = admins.getMembers()
  def addAdmin(id: LoginId): Future[Int] = admins.addMember(id)
  def removeAdmin(id: LoginId): Future[Int] = admins.removeMember(id)

  lazy val earlyAccess = new PermissionColumn("early_access")
  def getEarlyAccess(): Future[List[LoginId]] = earlyAccess.getMembers()
  def addEarlyAccess(id: LoginId): Future[Int] = earlyAccess.addMember(id)
  def removeEarlyAccess(id: LoginId): Future[Int] = earlyAccess.removeMember(id)

}
