package zio.elasticsearch.domain

import zio.elasticsearch.FieldAccessorBuilder
import zio.elasticsearch.data.GeoPoint
import zio.schema.{DeriveSchema, Schema}

import java.time.LocalDate

final case class TestDocument(
  stringField: String,
  subDocumentList: List[TestSubDocument],
  dateField: LocalDate,
  intField: Int,
  doubleField: Double,
  booleanField: Boolean,
  geoPointField: GeoPoint
)

object TestDocument {
  implicit val schema
    : Schema.CaseClass7[String, List[TestSubDocument], LocalDate, Int, Double, Boolean, GeoPoint, TestDocument] =
    DeriveSchema.gen[TestDocument]

  val (stringField, subDocumentList, dateField, intField, doubleField, booleanField, geoPointField) =
    schema.makeAccessors(FieldAccessorBuilder)
}
