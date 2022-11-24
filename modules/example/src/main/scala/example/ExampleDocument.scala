package example

import zio.schema.{DeriveSchema, Schema}

final case class ExampleDocument(id: String, count: Int)

object ExampleDocument {
  implicit val exampleDocumentSchema: Schema[ExampleDocument] = DeriveSchema.gen[ExampleDocument]
}
