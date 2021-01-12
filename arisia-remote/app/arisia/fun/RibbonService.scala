package arisia.fun

import java.util.concurrent.atomic.AtomicReference

import scala.util.Random
import arisia.db.DBService
import arisia.models.LoginId
import play.api.Logging
import play.api.libs.json.{Format, Json}
import doobie._
import doobie.implicits._

import scala.concurrent.{Future, ExecutionContext}

case class Ribbon(id: Int, ribbonText: String, colorFg: Option[String], colorBg: Option[String],
                  gradient: Option[String], imageFg: Option[String], imageBg: Option[String], secret: String, selfService: Boolean)
object Ribbon {
  implicit val fmt: Format[Ribbon] = Json.format

  val empty = Ribbon(0, "", None, None, None, None, None, "", false)
}

trait RibbonService {
  // API endpoints
  def assignRibbon(who: LoginId, ribbon: Int, secret: String): Future[Int]
  def orderRibbons(who: LoginId, ribbonIds: List[Int]): Future[Int]
  def getSelfServeRibbons(): List[Ribbon]
  def getRibbon(secret: String): Option[Ribbon]
  def getRibbon(id: Int): Option[Ribbon]
  def getRibbonsFor(who: LoginId): Future[List[Ribbon]]

  // CRUD endpoints
  def getAllRibbons(): List[Ribbon]
  def addRibbon(ribbon: Ribbon): Future[List[Ribbon]]
  def editRibbon(ribbon: Ribbon): Future[List[Ribbon]]
  def removeRibbon(id: Int): Future[List[Ribbon]]
}

class RibbonServiceImpl(
  dbService: DBService
)(
  implicit ec: ExecutionContext
) extends RibbonService with Logging {

  val _ribbonCache: AtomicReference[List[Ribbon]] = new AtomicReference(List.empty)

  def loadRibbons(): Future[List[Ribbon]] = {
    dbService.run(
      sql"""
           SELECT ribbonid, ribbon_text, color_fg, color_bg, gradient, image_fg, image_bg, secret, self_service
             FROM ribbons"""
        .query[Ribbon]
        .to[List]
    ).map { ribbons =>
      _ribbonCache.set(ribbons)
      ribbons
    }
  }

  // API endpoints
  def assignRibbon(who: LoginId, ribbonId: Int, secret: String): Future[Int] = {
    getRibbon(ribbonId) match {
      case Some(ribbon) if (ribbon.secret == secret) => {
        dbService.run(
          sql"""
           INSERT INTO member_ribbons
           (username, ribbonid, display_order)
           VALUES
           (${who.lower}, $ribbonId, order)
           WHERE order = (
             SELECT COUNT(*)
               FROM member_ribbons
              WHERE username = ${who.lower}
           )
           ON CONFLICT (username, ribbonid) DO NOTHING"""
            .update
            .run
        )
      }
      case _ => Future.successful(0)
    }
  }
  def orderRibbons(who: LoginId, ribbonIds: List[Int]): Future[Int] = {
    // There may be a way to do this in Postgres in one command, but I have no clue what it is, so we'll loop:
    def orderRibbonLoop(index: Int, remainingRibbons: List[Int]): Future[Int] = {
      if (remainingRibbons.isEmpty) {
        Future.successful(0)
      } else {
        val ribbonid = remainingRibbons.head
        dbService.run(
          sql"""
                 UPDATE member_ribbons
                    SET display_order = index
                  WHERE username = ${who.lower} AND ribbonid = $ribbonid"""
            .update
            .run
        ).flatMap(_ => orderRibbonLoop(index + 1, remainingRibbons.tail))
      }
    }

    orderRibbonLoop(0, ribbonIds)
  }
  def getSelfServeRibbons(): List[Ribbon] = {
    _ribbonCache.get().filter(_.selfService)
  }

  def getRibbon(secret: String): Option[Ribbon] = {
    _ribbonCache.get().find(_.secret == secret)
  }

  def getRibbonsFor(who: LoginId): Future[List[Ribbon]] = {
    dbService.run(
      sql"""
            SELECT ribbonid
              FROM member_ribbons
             ORDER BY display_order
             WHERE username = ${who.lower}
           """
        .query[Int]
        .to[List]
    ).map { ribbonIds =>
      ribbonIds.map(getRibbon(_)).flatten
    }
  }

  // CRUD endpoints
  def getAllRibbons(): List[Ribbon] = _ribbonCache.get()

  def getRibbon(id: Int): Option[Ribbon] = _ribbonCache.get().find(_.id == id)

  def addRibbon(ribbon: Ribbon): Future[List[Ribbon]] = {
    val secret = Random.alphanumeric.take(10).mkString
    dbService.run(
      sql"""
           INSERT INTO ribbons
           (ribbon_text, color_fg, color_bg, gradient, image_fg, image_bg, secret, self_service)
           VALUES
           (${ribbon.ribbonText}, ${ribbon.colorFg}, ${ribbon.colorBg}, ${ribbon.gradient}, ${ribbon.imageFg}, ${ribbon.imageBg}, $secret, ${ribbon.selfService})"""
        .update
        .run
    ).flatMap(_ => loadRibbons())
  }

  def editRibbon(ribbon: Ribbon): Future[List[Ribbon]] = {
    dbService.run(
      sql"""
           UPDATE ribbons
              SET ribbon_text = ${ribbon.ribbonText},
                  color_fg = ${ribbon.colorFg},
                  color_bg = ${ribbon.colorBg},
                  gradient = ${ribbon.gradient},
                  image_fg = ${ribbon.imageFg},
                  image_bg = ${ribbon.imageBg},
                  secret = ${ribbon.secret},
                  self_service = ${ribbon.selfService}
            WHERE ribbonid = ${ribbon.id}"""
        .update
        .run
    ).flatMap(_ => loadRibbons())
  }

  def removeRibbon(id: Int): Future[List[Ribbon]] = {
    dbService.run(
    sql"""
         DELETE FROM ribbons
          WHERE ribbonid = $id"""
      .update
      .run
    ).flatMap(_ => loadRibbons())
  }
}
