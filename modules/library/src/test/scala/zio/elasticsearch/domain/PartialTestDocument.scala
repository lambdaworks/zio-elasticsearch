package zio.elasticsearch.domain

import zio.elasticsearch.FieldAccessorBuilder
import zio.schema.{DeriveSchema, Schema}

final case class PartialTestDocument(
  stringField: String,
  subDocumentList: List[PartialTestSubDocument],
  intField: Int
)

object PartialTestDocument {
  implicit val schema: Schema.CaseClass3[String, List[PartialTestSubDocument], Int, PartialTestDocument] =
    DeriveSchema.gen[PartialTestDocument]

  val (stringField, subDocumentList, intField) = schema.makeAccessors(FieldAccessorBuilder)
}
