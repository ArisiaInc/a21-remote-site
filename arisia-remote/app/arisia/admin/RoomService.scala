package arisia.admin

import java.util.concurrent.atomic.AtomicReference

import arisia.db.DBService
import arisia.general.{LifecycleItem, LifecycleService}
import arisia.models.{ProgramItemLoc, ZoomRoom}
import arisia.util.Done
import play.api.Logging
import doobie._
import doobie.implicits._

import scala.concurrent.{Future, ExecutionContext}

/**
 * Management CRUD for Zoom Rooms.
 */
trait RoomService {
  def getRooms(): List[ZoomRoom]
  def addRoom(room: ZoomRoom): Future[Done]
  def editRoom(room: ZoomRoom): Future[Done]
  def removeRoom(id: Int): Future[Done]

  def getRoomForZambia(loc: ProgramItemLoc): Future[Option[ZoomRoom]]
}

class RoomServiceImpl(
  dbService: DBService,
  lifecycleService: LifecycleService
)(
  implicit ec: ExecutionContext
) extends RoomService with LifecycleItem with Logging
{
  val _roomCache: AtomicReference[List[ZoomRoom]] = new AtomicReference(List.empty)

  val lifecycleName = "RoomService"
  lifecycleService.register(this)
  override def init(): Future[Done] = {
    loadRooms()
  }

  private def loadRooms(): Future[Done] = {
    dbService.run(
      sql"""
            SELECT did, display_name, zoom_id, zambia_name, discord_name, manual, webinar
              FROM zoom_rooms"""
        .query[ZoomRoom]
        .to[List]
    ).map { rooms =>
      _roomCache.set(rooms)
      Done
    }
  }

  def getRooms(): List[ZoomRoom] = _roomCache.get()

  def addRoom(room: ZoomRoom): Future[Done] =
    dbService.run(
      sql"""
            INSERT INTO zoom_rooms
            (display_name, zoom_id, zambia_name, discord_name, manual, webinar)
            VALUES
            (${room.displayName}, ${room.zoomId}, ${room.zambiaName}, ${room.discordName}, ${room.isManual}, ${room.isWebinar})
           """
        .update
        .run
    ).flatMap(_ => loadRooms())

  def editRoom(room: ZoomRoom): Future[Done] =
    dbService.run(
      sql"""
           UPDATE zoom_rooms
              SET display_name = ${room.displayName},
                  zoom_id = ${room.zoomId},
                  zambia_name = ${room.zambiaName},
                  discord_name = ${room.discordName},
                  manual = ${room.isManual},
                  webinar = ${room.isWebinar}
            WHERE did = ${room.id}"""
        .update
        .run
    ).flatMap(_ => loadRooms())

  def getRoomForZambia(loc: ProgramItemLoc): Future[Option[ZoomRoom]] = {
    dbService.run(
      sql"""
            SELECT did, display_name, zoom_id, zambia_name, discord_name, manual, webinar
              FROM zoom_rooms
             WHERE zambia_name = ${loc.v}"""
        .query[ZoomRoom]
        .option
    )
  }

  def removeRoom(id: Int): Future[Done] = {
    dbService.run(
      sql"""
           DELETE FROM zoom_rooms
            WHERE did = $id"""
        .update
        .run
    ).flatMap(_ => loadRooms())
  }
}
