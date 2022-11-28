package zio.elasticsearch

import zio.json.ast.Json
import zio.json.{DeriveJsonDecoder, JsonDecoder, jsonField}

final case class ElasticGetResponse(
  found: Boolean,
  @jsonField("_source")
  source: Json
)

object ElasticGetResponse {
  implicit val decoder: JsonDecoder[ElasticGetResponse] = DeriveJsonDecoder.gen[ElasticGetResponse]
}
