package arisia.discord

import play.api.libs.json.{Format, Json}

case class DiscordUser(id: String, username: String, discriminator: String)
object DiscordUser {
  implicit val fmt: Format[DiscordUser] = Json.format
}

case class DiscordMember(user: DiscordUser)
object DiscordMember {
  implicit val fmt: Format[DiscordMember] = Json.format
}
