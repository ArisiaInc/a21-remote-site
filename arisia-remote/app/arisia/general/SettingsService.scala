package arisia.general

import arisia.db.DBService
import arisia.models.LoginUser
import play.api.Logging
import doobie._
import doobie.implicits._
import scala.concurrent.{Future, ExecutionContext}

trait SettingsService {
  def getSettings(who: LoginUser): Future[Map[String, String]]
  def addSettings(who: LoginUser, settings: Map[String, String]): Future[Int]
  def dropSetting(who: LoginUser, which: String): Future[Int]
}

class SettingsServiceImpl(
  dbService: DBService
)(
  implicit ec: ExecutionContext
) extends SettingsService with Logging {
  def getSettings(who: LoginUser): Future[Map[String, String]] = {
    dbService.run(
      sql"""
           SELECT k, v
             FROM user_settings
            WHERE username = ${who.id.lower}"""
        .query[(String, String)]
        .to[List]
        .map(_.toMap)
    )
  }

  def addSettings(who: LoginUser, settings: Map[String, String]): Future[Int] = {
    // It's probably possible to do this all in one shot, but it's easier for me to get the Scala right
    // than the SQL, and it's not a huge problem if we do a few extra calls:
    settings.foldLeft(Future.successful(0)) { case (prev, (k, v)) =>
      prev.flatMap { _ =>
        dbService.run(
          sql"""
            INSERT INTO user_settings
            (username, k, v)
            VALUES
            (${who.id.lower}, $k, $v)
            ON CONFLICT (username, k)
            DO UPDATE SET v = $v
           """
            .update
            .run
        )
      }
    }
  }

  def dropSetting(who: LoginUser, which: String): Future[Int] = {
    dbService.run(
      sql"""
           DELETE FROM user_settings
            WHERE username = ${who.id.lower} and k = $which"""
        .update
        .run
    )
  }
}