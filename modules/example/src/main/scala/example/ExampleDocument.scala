package example

import zio.schema.{DeriveSchema, Schema}

final case class ExampleDocument(id: String, name: String, count: Int)

object ExampleDocument {
  implicit val schema: Schema[ExampleDocument] = DeriveSchema.gen[ExampleDocument]
}
