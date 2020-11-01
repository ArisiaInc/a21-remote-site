import play.api.ApplicationLoader.Context
import play.api._
import com.softwaremill.macwire._
import _root_.controllers.{ControllerModule, AssetsComponents, AssetsConfiguration, AssetsMetadata, AssetsMetadataProvider}
import play.api.i18n.I18nComponents
import play.api.mvc.EssentialFilter
import router.Routes

/**
 * This is the master definition of the components in the application.
 *
 * This is defined using the "cake pattern": this object is comprised of a whole bunch of Scala traits, each of
 * which adds a bit of functionality. All put together, you wind up with one big object that is the entire application.
 *
 * (The cake pattern has its drawbacks, and don't think of it as the solution to all problems. But it works pretty
 * well for this sort of thing.)
 */
class PlayComponents(context: Context)
  extends BuiltInComponentsFromContext(context)
  with BuiltInComponents
  with I18nComponents
  with AssetsComponents
  with ControllerModule
{
  lazy val httpFilters: Seq[EssentialFilter] = Seq()

  lazy val router: Routes = {
    val prefix: String = "/"
    wire[Routes]
  }
}
