package example

import zio.json.{DeriveJsonEncoder, JsonEncoder}
import zio.schema.{DeriveSchema, Schema}

import java.time.LocalDateTime

final case class Repository(
  id: Option[String],
  organization: String,
  name: String,
  url: String,
  description: Option[String],
  lastCommitAt: LocalDateTime,
  stars: Int,
  forks: Int
)

object Repository {
  implicit val schema: Schema[Repository] = DeriveSchema.gen[Repository]

  implicit val encoder: JsonEncoder[Repository] = DeriveJsonEncoder.gen[Repository]
}
