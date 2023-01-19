package example.external.github.model

import zio.json.{DeriveJsonDecoder, JsonDecoder, jsonField}

final case class RepoResponse(
  id: Option[Int],
  name: String,
  url: String,
  description: Option[String],
  @jsonField("updated_at")
  updatedAt: String,
  @jsonField("stargazers_count")
  stars: Int,
  forks: Int,
  owner: RepoOwner
)

object RepoResponse {
  implicit val decoder: JsonDecoder[RepoResponse] = DeriveJsonDecoder.gen[RepoResponse]
}
