package zio.elasticsearch

import zio.json.ast.Json
import zio.json.{DeriveJsonDecoder, JsonDecoder, jsonField}

private[elasticsearch] final case class ElasticQueryResponse(
  @jsonField("hits")
  source: Json
)

private[elasticsearch] object ElasticQueryResponse {
  implicit val decoder: JsonDecoder[ElasticQueryResponse] = DeriveJsonDecoder.gen[ElasticQueryResponse]
}
