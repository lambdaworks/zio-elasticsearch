package example.external.github.model

import zio.json.{DeriveJsonDecoder, JsonDecoder, jsonField}

final case class RepoOwner(
  @jsonField("login")
  organization: String
)

object RepoOwner {
  implicit val decoder: JsonDecoder[RepoOwner] = DeriveJsonDecoder.gen[RepoOwner]
}
