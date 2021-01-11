package arisia.schedule

import arisia.db.DBService
import arisia.models.{ProgramItemId, LoginId}
import arisia.util.Done
import play.api.Logging
import doobie._
import doobie.implicits._

import scala.concurrent.Future

/**
 * A simple service for tracking which program items a user has starred.
 */
trait StarService {
  def addStar(who: LoginId, which: ProgramItemId): Future[Int]
  def removeStar(who: LoginId, which: ProgramItemId): Future[Int]
  def getStars(who: LoginId): Future[List[ProgramItemId]]
}

class StarServiceImpl(
  dbService: DBService
) extends StarService with Logging {
  def addStar(who: LoginId, which: ProgramItemId): Future[Int] = {
    val query =
      sql"""INSERT INTO starred_items
           (login_id, item_id)
           VALUES (${who.lower}, ${which.v})"""
    dbService.run(
      query.
        update.
        run
    )
  }

  def removeStar(who: LoginId, which: ProgramItemId): Future[Int] = {
    val query =
      sql"""DELETE FROM starred_items
           WHERE login_id = ${who.lower} AND item_id = ${which.v}"""
    dbService.run(
      query.
        update.
        run
    )
  }

  def getStars(who: LoginId): Future[List[ProgramItemId]] = {
    val query =
      sql"""SELECT item_id
           FROM starred_items
           WHERE login_id = ${who.lower}"""
    dbService.run(
      query.
        query[ProgramItemId]
        .to[List]
    )
  }
}
