package zio.elasticsearch

import zio.json.ast.Json
import zio.json.{DeriveJsonDecoder, JsonDecoder, jsonField}

final case class ElasticResponse(
  @jsonField("_index")
  index: String,
  @jsonField("_type")
  `type`: String,
  @jsonField("_id")
  id: String,
  @jsonField("_version")
  version: Int,
  @jsonField("_seq_no")
  seqNo: Int,
  @jsonField("_primary_term")
  primaryTerm: Int,
  found: Boolean,
  @jsonField("_source")
  source: Json
)

object ElasticResponse {
  implicit val decoder: JsonDecoder[ElasticResponse] = DeriveJsonDecoder.gen[ElasticResponse]
}
