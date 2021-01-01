package arisia.admin

import arisia.db.DBService
import arisia.models.ZoomRoom
import play.api.Logging
import doobie._
import doobie.implicits._

import scala.concurrent.Future

/**
 * Management CRUD for Zoom Rooms.
 */
trait RoomService {
  def getRooms(): Future[List[ZoomRoom]]
  def addRoom(room: ZoomRoom): Future[Int]
  def editRoom(room: ZoomRoom): Future[Int]
}

class RoomServiceImpl(
  dbService: DBService
) extends RoomService with Logging
{
  def getRooms(): Future[List[ZoomRoom]] =
    dbService.run(
      sql"""
            SELECT did, display_name, zoom_id, zambia_name, manual, webinar
              FROM zoom_rooms"""
        .query[ZoomRoom]
        .to[List]
    )

  def addRoom(room: ZoomRoom): Future[Int] =
    dbService.run(
      sql"""
            INSERT INTO zoom_rooms
            (display_name, zoom_id, zambia_name, manual, webinar)
            VALUES
            (${room.displayName}, ${room.zoomId}, ${room.zambiaName}, ${room.isManual}, ${room.isWebinar})
           """
        .update
        .run
    )

  def editRoom(room: ZoomRoom): Future[Int] =
    dbService.run(
      sql"""
           UPDATE zoom_rooms
              SET display_name = ${room.displayName},
                  zoom_id = ${room.zoomId},
                  zambia_name = ${room.zambiaName},
                  manual = ${room.isManual},
                  webinar = ${room.isWebinar}
            WHERE did = ${room.id}"""
        .update
        .run
    )
}