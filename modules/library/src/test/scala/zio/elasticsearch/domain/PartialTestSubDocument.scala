package zio.elasticsearch.domain

import zio.elasticsearch.FieldAccessorBuilder
import zio.schema.{DeriveSchema, Schema}

final case class PartialTestSubDocument(
  stringField: String,
  intFieldList: List[Int]
)

object PartialTestSubDocument {
  implicit val schema: Schema.CaseClass2[String, List[Int], PartialTestSubDocument] =
    DeriveSchema.gen[PartialTestSubDocument]

  val (stringField, intFieldList) = schema.makeAccessors(FieldAccessorBuilder)
}
