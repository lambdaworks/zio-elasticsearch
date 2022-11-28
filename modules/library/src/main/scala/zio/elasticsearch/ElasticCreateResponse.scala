package zio.elasticsearch

import zio.json.ast.Json
import zio.json.{DeriveJsonDecoder, JsonDecoder, jsonField}

final case class ElasticCreateResponse(
  @jsonField("_index")
  index: String,
  @jsonField("_type")
  `type`: String,
  @jsonField("_id")
  id: String,
  @jsonField("_version")
  version: Int,
  @jsonField("result")
  result: String,
  @jsonField("_shards")
  shards: Json
)

object ElasticCreateResponse {
  implicit val decoder: JsonDecoder[ElasticCreateResponse] =
    DeriveJsonDecoder.gen[ElasticCreateResponse]
}
