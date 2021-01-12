package arisia.fun

case class Ribbon(id: Int, ribbonText: String, colorFg: Option[String], colorBg: Option[String],
                  gradient: Option[String], imageFg: Option[String], imageBg: Option[String], secret: String, selfService: Boolean)
object Ribbon {
    implicit val fmt: Format[Ribbon] = Json.format
}

trait RibbonService {
    // API endpoints
    def assignRibbon(who: LoginId, ribbon: Int, secret: String): Future[Int]
    def orderRibbons(who: LoginId, ribbonIds: List[Int])
    def getRibbons(selfService: Boolean): List[Ribbon]
    def getRibbon(secret: String): Option[Ribbon]

    // CRUD endpoints
}
