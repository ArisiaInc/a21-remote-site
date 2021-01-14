package arisia.discord

import arisia.auth.MembershipType
import arisia.models.{BadgeNumber, LoginName, LoginUser, LoginId}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class DiscordServiceSpec extends AnyWordSpec with Matchers {
  "DiscordService" should {
    "be able to generate and validate secrets" in {
      val user = LoginUser(
        LoginId("foo"),
        LoginName("Foo Bar"),
        BadgeNumber("29309"),
        false,
        MembershipType.AdultStandard
      )
      val secretKey = "lsdflisdpisdf;kasd;lkas;lk"
      val secret = DiscordService.generateAssistSecret(secretKey)(user)
      DiscordService.validateAssistSecret(secretKey)(secret) shouldBe true
    }
  }
}
