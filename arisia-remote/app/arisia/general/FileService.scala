package arisia.general

import arisia.db.DBService
import play.api.Logging

trait FileService {

}

class FileServiceImpl(
  dbService: DBService
) extends FileService with Logging {

}