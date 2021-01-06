package arisia.fun

import java.util.concurrent.atomic.AtomicReference

import arisia.db.DBService
import arisia.general.{LifecycleService, LifecycleItem}
import arisia.models.LoginId
import arisia.util.Done
import play.api.Logging
import doobie._
import doobie.implicits._
import play.api.libs.json._
import play.api.libs.functional.syntax._

import scala.concurrent.{Future, ExecutionContext}

case class Duck(id: Int, imageUrl: String, altText: String, link: String, hint: Option[String], requestingUrl: String)
object Duck {
  // We shouldn't be sending the requestingUrl field publicly, so we need to hand-craft the serializer instead
  // of just using the Json.writes macro here:
  implicit val writes: Writes[Duck] = (d: Duck) =>
    JsObject(Map(
      "id" -> JsNumber(d.id),
      "imageUrl" -> JsString(d.imageUrl),
      "altText" -> JsString(d.altText),
      "link" -> JsString(d.link),
      "hint" -> JsString(d.hint.getOrElse(""))
    ))

  def empty = Duck(0, "", "", "", None, "")
}

/**
 * Functionality for the Duck Hunt.
 */
trait DuckService {
  // API endpoints
  def getDucks(): List[Duck]
  def getDuck(id: Int): Option[Duck]
  def assignDuck(who: LoginId, duck: Int, from: String): Future[Int]
  def dropDuck(who: LoginId, duck: Int): Future[Int]

  // CRUD endpoints
  def addDuck(duck: Duck): Future[List[Duck]]
  def editDuck(duck: Duck): Future[List[Duck]]
  def removeDuck(id: Int): Future[List[Duck]]
}

class DuckServiceImpl(
  dbService: DBService,
  lifecycleService: LifecycleService
)(
  implicit ec: ExecutionContext
) extends DuckService with Logging with LifecycleItem {

  val lifecycleName = "DuckService"
  lifecycleService.register(this)
  override def init(): Future[Done] = {
    loadDucks().map { _ => Done }
  }

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

  def assignDuck(who: LoginId, duckId: Int, from: String): Future[Int] = {
    // Need to validate that the request is coming from the right URL:
    getDuck(duckId) match {
      case Some(duck) => {
        if (duck.requestingUrl == from) {
          // All looks good:
          dbService.run(
            sql"""
           INSERT INTO member_ducks
           (username, duck_id)
           VALUES
           (${who.v}, $duckId)"""
              .update
              .run
          )
        } else {
          // Somehow got a request from the wrong place, which seems hinky:
          val error = s"Got a request for Duck $duckId from incorrect location $from!"
          logger.warn(error)
          Future.failed(new Exception(error))
        }
      }
      case None => {
        val error = s"Somehow got a claim for unknown Duck $duckId, from $from!"
        logger.error(error)
        Future.failed(new Exception(error))
      }
    }
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
