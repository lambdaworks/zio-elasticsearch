package zio.elasticsearch.domain

import zio.elasticsearch.FieldAccessorBuilder
import zio.schema.{DeriveSchema, Schema}

import java.time.LocalDate

final case class TestDoc2(subDocument: TestSubDocument)

object TestDoc2 {
  implicit val schema: Schema.CaseClass1[TestSubDocument, TestDoc2] = DeriveSchema.gen[TestDoc2]
}

final case class TestDocument(
  stringField: String,
  subDocumentList: List[TestSubDocument],
  dateField: LocalDate,
  intField: Int,
  doubleField: Double,
  booleanField: Boolean,
  locationField: Location
)

object TestDocument {
  implicit val schema
    : Schema.CaseClass7[String, List[TestSubDocument], LocalDate, Int, Double, Boolean, Location, TestDocument] =
    DeriveSchema.gen[TestDocument]

  val (stringField, subDocumentList, dateField, intField, doubleField, booleanField, locationField) =
    schema.makeAccessors(FieldAccessorBuilder)
}
