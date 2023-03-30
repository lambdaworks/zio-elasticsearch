package zio.elasticsearch.utils

import zio.elasticsearch.FieldAccessorBuilder
import zio.schema.{DeriveSchema, Schema}

import java.time.LocalDate

final case class TestDocument(
  stringField: String,
  subDocumentList: List[TestSubDocument],
  dateField: LocalDate,
  intField: Int,
  doubleField: Double
)

object TestDocument {
  implicit val schema: Schema.CaseClass5[String, List[TestSubDocument], LocalDate, Int, Double, TestDocument] =
    DeriveSchema.gen[TestDocument]

  val (stringField, subDocumentList, dateField, intField, doubleField) = schema.makeAccessors(FieldAccessorBuilder)
}
