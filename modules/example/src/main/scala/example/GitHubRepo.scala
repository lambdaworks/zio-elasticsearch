package example

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder, jsonField}
import zio.schema.{DeriveSchema, Schema}

import java.time.LocalDateTime

final case class GitHubRepo(
  id: Option[String],
  organization: String,
  name: String,
  url: String,
  description: Option[String],
  lastCommitAt: LocalDateTime,
  stars: Int,
  forks: Int
)

object GitHubRepo {
  def apply(repoResponse: GitHubRepoResponse): GitHubRepo =
    GitHubRepo(
      id = repoResponse.id.map(_.toString),
      organization = repoResponse.owner.organization,
      name = repoResponse.name,
      url = repoResponse.url,
      description = repoResponse.description,
      lastCommitAt = LocalDateTime.parse(repoResponse.updatedAt.dropRight(1)),
      stars = repoResponse.stars,
      forks = repoResponse.forks
    )
  implicit val schema: Schema[GitHubRepo] = DeriveSchema.gen[GitHubRepo]

  implicit val decoder: JsonDecoder[GitHubRepo] = DeriveJsonDecoder.gen[GitHubRepo]

  implicit val encoder: JsonEncoder[GitHubRepo] = DeriveJsonEncoder.gen[GitHubRepo]
}

final case class GitHubRepoResponse(
  id: Option[Int],
  name: String,
  url: String,
  description: Option[String],
  @jsonField("updated_at")
  updatedAt: String,
  @jsonField("stargazers_count")
  stars: Int,
  forks: Int,
  owner: GitHubRepoOwner
)

final case class GitHubRepoOwner(
  @jsonField("login")
  organization: String
)

object GitHubRepoResponse {
  implicit val decoder: JsonDecoder[GitHubRepoResponse] = DeriveJsonDecoder.gen[GitHubRepoResponse]
}

object GitHubRepoOwner {
  implicit val decoder: JsonDecoder[GitHubRepoOwner] = DeriveJsonDecoder.gen[GitHubRepoOwner]
}
