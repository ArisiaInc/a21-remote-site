package arisia.util

import play.api.libs.json._

object JsUtils {
  def stringReads[T](f: String => T): Reads[T] = new Reads[T] {
    override def reads(json: JsValue): JsResult[T] = {
      json match {
        case JsString(str) => {
          try {
            JsSuccess(f(str))
          } catch {
            case ex: Exception => {
              // TODO: log this error!
              JsError()
            }
          }
        }
        case _ => {
          // TODO: log this error!
          JsError()
        }
      }
    }
  }
}
