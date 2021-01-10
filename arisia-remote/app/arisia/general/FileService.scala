package arisia.general

import arisia.db.DBService
import play.api.Logging
import doobie._
import doobie.implicits._

import scala.concurrent.{Future, ExecutionContext}

trait FileService {
  def setFile(tpe: FileType, content: String): Future[Int]
}

class FileServiceImpl(
  dbService: DBService
)(
  implicit ec: ExecutionContext
) extends FileService with Logging {
  def setFile(tpe: FileType, content: String): Future[Int] = {
    dbService.run(
      sql"""
           INSERT INTO text_files
           (name, value)
           VALUES
           (${tpe.value}, $content)
           ON CONFLICT (name)
           DO UPDATE SET value = $content"""
        .update
        .run
    )
  }
}
