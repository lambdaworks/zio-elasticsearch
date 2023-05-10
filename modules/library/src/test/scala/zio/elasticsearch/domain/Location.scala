package zio.elasticsearch.domain

import zio.elasticsearch.FieldAccessorBuilder
import zio.schema.{DeriveSchema, Schema}

case class Location(lat: Double, lon: Double)

object Location {
  implicit val schema: Schema.CaseClass2[Double, Double, Location] = DeriveSchema.gen[Location]

  val (latField, lonField) = schema.makeAccessors(FieldAccessorBuilder)
}
