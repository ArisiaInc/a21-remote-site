package arisia

import play.api._

/**
 * This is the top level of the application -- at startup time, it builds the arisia.PlayComponents class, which is
 * really the definition of the app.
 */
class ArisiaApplicationLoader extends ApplicationLoader {
  override def load(context: ApplicationLoader.Context) = {
    LoggerConfigurator(context.environment.classLoader).foreach {
      _.configure(context.environment)
    }

    // TODO (#54): load the application secret from the environment, instead of using this hardcoded one:
    val secret = "changeme"
    val secrets: Map[String, String] = Map(
      "play.crypto.secret" -> secret
    )
    val configuration = Configuration.from(secrets).withFallback(context.initialConfiguration)
    val configuredContext = context.copy(initialConfiguration = configuration)

    val components = new PlayComponents(configuredContext)
    components.application
  }
}
