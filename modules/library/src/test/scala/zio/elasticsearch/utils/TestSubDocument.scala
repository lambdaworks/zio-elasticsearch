package zio.elasticsearch.utils

import zio.elasticsearch.FieldAccessorBuilder
import zio.schema.{DeriveSchema, Schema}

final case class TestSubDocument(
  stringField: String,
  nestedField: NestedField,
  intField: Int,
  intFieldList: List[Int]
)

object TestSubDocument {
  implicit val schema: Schema.CaseClass4[String, NestedField, Int, List[Int], TestSubDocument] =
    DeriveSchema.gen[TestSubDocument]

  val (stringField, nestedField, intField, intFieldList) = schema.makeAccessors(FieldAccessorBuilder)
}

final case class NestedField(stringField: String, longField: Long)

object NestedField {
  implicit val schema: Schema.CaseClass2[String, Long, NestedField] = DeriveSchema.gen[NestedField]

  val (stringField, longField) = schema.makeAccessors(FieldAccessorBuilder)
}
