package arisia.auth

import com.softwaremill.macwire._

trait AuthModule {
  lazy val loginService: LoginService = wire[LoginServiceImpl]
}
