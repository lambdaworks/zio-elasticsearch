package zio.elasticsearch.domain

import zio.elasticsearch.FieldAccessorBuilder
import zio.schema.{DeriveSchema, Schema}

import java.time.LocalDate

final case class TestDocument(
  stringField: String,
  subDocumentList: List[TestSubDocument],
  dateField: LocalDate,
  intField: Int,
  doubleField: Double,
  booleanField: Boolean
)

object TestDocument {
  implicit val schema: Schema.CaseClass6[String, List[TestSubDocument], LocalDate, Int, Double, Boolean, TestDocument] =
    DeriveSchema.gen[TestDocument]

  val (stringField, subDocumentList, dateField, intField, doubleField, booleanField) =
    schema.makeAccessors(FieldAccessorBuilder)
}
