package arisia.fun

import java.util.concurrent.atomic.AtomicReference

import arisia.db.DBService
import arisia.models.LoginId
import play.api.Logging
import doobie._
import doobie.implicits._
import play.api.libs.json.{Format, Json}

import scala.concurrent.{Future, ExecutionContext}

case class Duck(id: Int, imageUrl: String, altText: String, link: String, hint: Option[String], requestingUrl: Option[String])
object Duck {
  implicit val fmt: Format[Duck] = Json.format
  def empty = Duck(0, "", "", "", None, None)
}

/**
 * Functionality for the Duck Hunt.
 */
trait DuckService {
  // API endpoints
  def getDucks(): List[Duck]
  def getDuck(id: Int): Option[Duck]
  def assignDuck(who: LoginId, duck: Int): Future[Int]
  def dropDuck(who: LoginId, duck: Int): Future[Int]

  // CRUD endpoints
  def addDuck(duck: Duck): Future[List[Duck]]
  def editDuck(duck: Duck): Future[List[Duck]]
  def removeDuck(id: Int): Future[List[Duck]]
}

class DuckServiceImpl(
  dbService: DBService
)(
  implicit ec: ExecutionContext
) extends DuckService with Logging {

  // Yes, we're maintaining a high-performance cache of ducks. Why not...
  val _duckCache: AtomicReference[List[Duck]] = new AtomicReference(List.empty)

  def loadDucks(): Future[List[Duck]] = {
    dbService.run(
      sql"""
            SELECT did, image, alt, link, hint, requesting_url
            FROM ducks
           """
        .query[Duck]
        .to[List]
    ).map { ducks =>
      _duckCache.set(ducks)
      ducks
    }
  }

  def getDucks(): List[Duck] = _duckCache.get()

  def getDuck(id: Int): Option[Duck] = {
    _duckCache.get().find(_.id == id)
  }

  def assignDuck(who: LoginId, duck: Int): Future[Int] = {
    dbService.run(
      sql"""
           INSERT INTO member_ducks
           (username, duck_id)
           VALUES
           (${who.v}, $duck)"""
        .update
        .run
    )
  }
  def dropDuck(who: LoginId, duck: Int): Future[Int] = {
    dbService.run(
      sql"""
           DELETE FROM member_ducks
            WHERE username = ${who.v} AND duck_id = $duck"""
        .update
        .run
    )
  }

  // CRUD endpoints
  def addDuck(duck: Duck): Future[List[Duck]] = {
    dbService.run(
      sql"""
           INSERT INTO ducks
           (image, alt, link, hint, requesting_url)
           VALUES
           (${duck.imageUrl}, ${duck.altText}, ${duck.link}, ${duck.hint}, ${duck.requestingUrl})"""
        .update
        .run
    ).flatMap(_ => loadDucks())
  }
  def editDuck(duck: Duck): Future[List[Duck]] = {
    dbService.run(
      sql"""
           UPDATE duck
              SET image = ${duck.imageUrl}
                  alt = ${duck.altText}
                  link = ${duck.link}
                  hint = ${duck.hint}
                  requesting_url = ${duck.requestingUrl}
            WHERE did = ${duck.id}"""
        .update
        .run
    ).flatMap(_ => loadDucks())
  }
  def removeDuck(id: Int): Future[List[Duck]] = {
    dbService.run(
      sql"""
           DELETE FROM ducks
            WHERE did = $id"""
        .update
        .run
    ).flatMap(_ => loadDucks())
  }
}
