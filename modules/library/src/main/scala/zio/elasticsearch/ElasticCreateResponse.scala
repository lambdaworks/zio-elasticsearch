package zio.elasticsearch

import zio.json.{DeriveJsonDecoder, JsonDecoder, jsonField}

private[elasticsearch] final case class ElasticCreateResponse(
  @jsonField("_id")
  id: String
)

private[elasticsearch] object ElasticCreateResponse {
  implicit val decoder: JsonDecoder[ElasticCreateResponse] = DeriveJsonDecoder.gen[ElasticCreateResponse]
}
