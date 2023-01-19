package example

import example.external.github.model.RepoResponse
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}
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
  def fromResponse(response: RepoResponse): GitHubRepo =
    GitHubRepo(
      id = response.id.map(_.toString),
      organization = response.owner.organization,
      name = response.name,
      url = response.url,
      description = response.description,
      lastCommitAt = LocalDateTime.parse(response.updatedAt.dropRight(1)),
      stars = response.stars,
      forks = response.forks
    )

  implicit val schema: Schema[GitHubRepo] = DeriveSchema.gen[GitHubRepo]

  implicit val decoder: JsonDecoder[GitHubRepo] = DeriveJsonDecoder.gen[GitHubRepo]

  implicit val encoder: JsonEncoder[GitHubRepo] = DeriveJsonEncoder.gen[GitHubRepo]
}
