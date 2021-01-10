package arisia.controllers

import akka.util.ByteString
import arisia.auth.LoginService

import scala.concurrent.ExecutionContext
import play.api.i18n.I18nSupport
import play.api.mvc.{BaseController, ControllerComponents, EssentialAction}
import play.api.data._
import play.api.data.Forms._
import play.api.libs.streams.Accumulator
import play.core.Paths
import play.mvc.Http.MultipartFormData.{FileInfo, FilePart}

class FileController(
  val controllerComponents: ControllerComponents,
  val loginService: LoginService
)(
  implicit val ec: ExecutionContext
) extends BaseController
  with AdminControllerFuncs
  with I18nSupport
{
  type FilePartHandler[A] = FileInfo => Accumulator[ByteString, FilePart[A]]

  def handleFilePartAsFile: FilePartHandler[File] = ???
//  {
//    case FileInfo(partName, filename, contentType, dispositionType) =>
//      val perms       = java.util.EnumSet.of(OWNER_READ, OWNER_WRITE)
//      val attr        = PosixFilePermissions.asFileAttribute(perms)
//      val path        = JFiles.createTempFile("multipartBody", "tempFile", attr)
//      val file        = path.toFile
//      val fileSink    = FileIO.toPath(path)
//      val accumulator = Accumulator(fileSink)
//      accumulator.map {
//        case IOResult(count, status) =>
//          FilePart(partName, filename, contentType, file, count, dispositionType)
//      }(ec)
//  }

  def uploadCustom = Action(parse.multipartFormData(handleFilePartAsFile)) { request =>
    val fileOption = request.body.file("name").map {
      case FilePart(key, filename, contentType, file, fileSize, dispositionType) =>
        file.toPath
    }

    Ok(s"File uploaded: $fileOption")
  }}
