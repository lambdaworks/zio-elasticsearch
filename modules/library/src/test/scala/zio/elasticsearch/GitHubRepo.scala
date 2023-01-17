package zio.elasticsearch

import zio.json.{DeriveJsonEncoder, JsonEncoder}
import zio.schema.{DeriveSchema, Schema}

final case class GitHubRepo(
  id: Option[String],
  organization: String,
  name: String,
  stars: Int,
  forks: Int
)

object GitHubRepo {
  implicit val schema: Schema[GitHubRepo] = DeriveSchema.gen[GitHubRepo]

  implicit val encoder: JsonEncoder[GitHubRepo] = DeriveJsonEncoder.gen[GitHubRepo]
}
