package zio.elasticsearch

import zio.json.{DeriveJsonDecoder, JsonDecoder}

final case class JsonTestingClass(
  customer_first_name: String,
  customer_last_name: String,
  manufacturer: List[String],
  taxful_total_price: Double,
  currency: String,
  order_id: BigInt
)

object JsonTestingClass {
  implicit val decoder: JsonDecoder[JsonTestingClass] = DeriveJsonDecoder.gen[JsonTestingClass]
}

final case class ElasticResponseClass(
  _index: String,
  _type: String,
  _id: String,
  _version: Int,
  _seq_no: Int,
  _primary_term: Int,
  found: Boolean,
  _source: JsonTestingClass
)

object ElasticResponseClass {
  implicit val decoder: JsonDecoder[ElasticResponseClass] = DeriveJsonDecoder.gen[ElasticResponseClass]
}
