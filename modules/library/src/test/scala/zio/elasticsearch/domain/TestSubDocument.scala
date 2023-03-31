package zio.elasticsearch.domain

import zio.elasticsearch.FieldAccessorBuilder
import zio.schema.{DeriveSchema, Schema}

final case class TestSubDocument(
  stringField: String,
  nestedField: TestNestedField,
  intField: Int,
  intFieldList: List[Int]
)

object TestSubDocument {
  implicit val schema: Schema.CaseClass4[String, TestNestedField, Int, List[Int], TestSubDocument] =
    DeriveSchema.gen[TestSubDocument]

  val (stringField, nestedField, intField, intFieldList) = schema.makeAccessors(FieldAccessorBuilder)
}
