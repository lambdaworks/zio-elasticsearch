package example

import zio.schema.{DeriveSchema, Schema}

import java.time.LocalDateTime

final case class Repository(
  name: String,
  url: String,
  description: Option[String],
  lastCommitAt: LocalDateTime,
  stars: Int,
  forks: Int
)

object Repository {
  implicit val schema: Schema[Repository] = DeriveSchema.gen[Repository]
}
