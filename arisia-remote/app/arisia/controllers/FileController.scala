package arisia.controllers

import akka.stream.scaladsl.Sink
import akka.util.ByteString
import arisia.auth.LoginService
import arisia.general.{FileService, FileType}

import scala.concurrent.ExecutionContext
import play.api.i18n.I18nSupport
import play.api.mvc.{ControllerComponents, BodyParser, BaseController, EssentialAction, MultipartFormData}
import play.api.data._
import play.api.data.Forms._
import play.api.libs.streams.Accumulator
import play.api.mvc.MultipartFormData.FilePart
import play.core.Paths
import play.core.parsers.Multipart.{FilePartHandler, FileInfo}

class FileController(
  val controllerComponents: ControllerComponents,
  val loginService: LoginService,
  fileService: FileService
)(
  implicit val ec: ExecutionContext
) extends BaseController
  with AdminControllerFuncs
  with I18nSupport {

  def manageMetadata(): EssentialAction = Action { implicit request =>
    Ok(arisia.views.html.uploadMetadata())
  }

  def byteStringFilePartHandler: FilePartHandler[ByteString] = {
    case FileInfo(partName, filename, contentType, dispositionType) =>
      Accumulator(Sink.fold[ByteString, ByteString](ByteString()) { (accumulator, data) =>
        accumulator ++ data
      }.mapMaterializedValue(fbs => fbs.map(bs => {
        FilePart(partName, filename, contentType, bs)
      })))
  }

  def multipartFormDataAsBytes: BodyParser[MultipartFormData[ByteString]] =
    controllerComponents.parsers.multipartFormData(byteStringFilePartHandler)

  def uploadBase(tpe: FileType): EssentialAction = adminsOnlyAsync(multipartFormDataAsBytes) { adminInfo =>
    val request = adminInfo.request
    // Rather than farting around with the terribly sophisticated and terribly hard-to-use multipart machinery
    // built into Play, we're doing this as dead-simply as we can:
    val fileParts: Seq[FilePart[ByteString]] = request.body.files
    val fullByteString: ByteString = fileParts.foldLeft(ByteString.empty) { (current, next) =>
      current ++ next.ref
    }
    val body: String = fullByteString.utf8String
    fileService.setFile(tpe, body).map { _ =>
      Redirect(routes.AdminController.home())
    }
  }

  def uploadArtshowMetadata(): EssentialAction = uploadBase(FileType.ArtshowMetadata)
  def uploadDealersMetadata(): EssentialAction = uploadBase(FileType.DealersMetadata)
  def uploadGamingMetadata(): EssentialAction = uploadBase(FileType.GamingMetadata)
  def uploadEventsMetadata(): EssentialAction = uploadBase(FileType.EventsMetadata)

  def getMetadataBase(tpe: FileType): EssentialAction = Action.async { implicit request =>
    fileService.getFile(tpe).map {
      _ match {
        case Some(metadata) => Ok(metadata)
        case _ => NotFound("")
      }
    }
  }

  def getArtshowMetadata(): EssentialAction = getMetadataBase(FileType.ArtshowMetadata)
  def getDealersMetadata(): EssentialAction = getMetadataBase(FileType.DealersMetadata)
  def getGamingMetadata(): EssentialAction = getMetadataBase(FileType.GamingMetadata)
  def getEventsMetadata(): EssentialAction = getMetadataBase(FileType.EventsMetadata)

}
