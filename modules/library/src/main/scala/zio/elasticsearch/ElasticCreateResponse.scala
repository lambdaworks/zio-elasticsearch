package zio.elasticsearch

import zio.json.{DeriveJsonDecoder, JsonDecoder, jsonField}

final case class ElasticCreateResponse(
  @jsonField("_id")
  id: String
)

object ElasticCreateResponse {
  implicit val decoder: JsonDecoder[ElasticCreateResponse] =
    DeriveJsonDecoder.gen[ElasticCreateResponse]
}
