package example

import example.external.github.model.RepoResponse
import zio.json.{DeriveJsonEncoder, JsonEncoder}
import zio.schema.{DeriveSchema, Schema}

import java.time.{Instant, LocalDateTime, ZoneId}

final case class GitHubRepo(
  id: String,
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
      id = response.id.toString,
      organization = response.owner.organization,
      name = response.name,
      url = response.url,
      description = response.description,
      lastCommitAt = LocalDateTime.ofInstant(Instant.parse(response.updatedAt), ZoneId.systemDefault()),
      stars = response.stars,
      forks = response.forks
    )

  implicit val schema: Schema[GitHubRepo] = DeriveSchema.gen[GitHubRepo]

  implicit val encoder: JsonEncoder[GitHubRepo] = DeriveJsonEncoder.gen[GitHubRepo]
}
