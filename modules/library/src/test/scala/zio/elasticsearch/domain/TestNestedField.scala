package zio.elasticsearch.domain

import zio.elasticsearch.FieldAccessorBuilder
import zio.schema.{DeriveSchema, Schema}

final case class TestNestedField(stringField: String, longField: Long)

object TestNestedField {
  implicit val schema: Schema.CaseClass2[String, Long, TestNestedField] = DeriveSchema.gen[TestNestedField]

  val (stringField, longField) = schema.makeAccessors(FieldAccessorBuilder)
}
