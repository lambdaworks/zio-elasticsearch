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
  geoPointField: GeoPoint,
  vectorField: List[Int]
)

object TestDocument {
  implicit val schema: Schema.CaseClass8[
    String,
    List[TestSubDocument],
    LocalDate,
    Int,
    Double,
    Boolean,
    GeoPoint,
    List[Int],
    TestDocument
  ] = DeriveSchema.gen[TestDocument]

  val (stringField, subDocumentList, dateField, intField, doubleField, booleanField, geoPointField, vectorField) =
    schema.makeAccessors(FieldAccessorBuilder)
}
