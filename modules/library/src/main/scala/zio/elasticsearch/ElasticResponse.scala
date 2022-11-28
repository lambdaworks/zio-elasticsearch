package zio.elasticsearch

import zio.json.ast.Json
import zio.json.{DeriveJsonDecoder, JsonDecoder, jsonField}

final case class ElasticResponse(
  found: Boolean,
  @jsonField("_source")
  source: Json
)

object ElasticResponse {
  implicit val decoder: JsonDecoder[ElasticResponse] = DeriveJsonDecoder.gen[ElasticResponse]
}
