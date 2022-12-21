package example

import zio.json.{DeriveJsonEncoder, JsonEncoder}
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
  implicit val schema: Schema[GitHubRepo] = DeriveSchema.gen[GitHubRepo]

  implicit val encoder: JsonEncoder[GitHubRepo] = DeriveJsonEncoder.gen[GitHubRepo]
}
