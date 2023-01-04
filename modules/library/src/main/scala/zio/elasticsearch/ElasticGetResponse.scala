package zio.elasticsearch

import zio.json.ast.Json
import zio.json.{DeriveJsonDecoder, JsonDecoder, jsonField}

private[elasticsearch] final case class ElasticGetResponse(
  @jsonField("_source")
  source: Json
)

private[elasticsearch] object ElasticGetResponse {
  implicit val decoder: JsonDecoder[ElasticGetResponse] = DeriveJsonDecoder.gen[ElasticGetResponse]
}
