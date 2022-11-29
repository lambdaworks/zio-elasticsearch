package zio.elasticsearch

import zio.json.{DeriveJsonDecoder, JsonDecoder}

private[elasticsearch] final case class ElasticDeleteResponse(
  result: String
)

private[elasticsearch] object ElasticDeleteResponse {
  implicit val decoder: JsonDecoder[ElasticDeleteResponse] = DeriveJsonDecoder.gen[ElasticDeleteResponse]
}
