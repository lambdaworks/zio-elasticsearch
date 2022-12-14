package zio.elasticsearch

import zio.json.ast.Json
import zio.json.{DeriveJsonDecoder, JsonDecoder, jsonField}

private[elasticsearch] final case class ElasticQueryResponse(
  took: Int,
  @jsonField("timed_out")
  timedOut: Boolean,
  @jsonField("_shards")
  shards: Shards,
  hits: Hits
)

private[elasticsearch] object ElasticQueryResponse {
  implicit val decoder: JsonDecoder[ElasticQueryResponse] = DeriveJsonDecoder.gen[ElasticQueryResponse]
}

private[elasticsearch] final case class Shards(
  total: Int,
  successful: Int,
  skipped: Int,
  failed: Int
)

private[elasticsearch] object Shards {
  implicit val decoder: JsonDecoder[Shards] = DeriveJsonDecoder.gen[Shards]
}

private[elasticsearch] final case class Hits(
  total: Total,
  @jsonField("max_score")
  maxScore: Double,
  hits: List[Item]
)

private[elasticsearch] object Hits {
  implicit val decoder: JsonDecoder[Hits] = DeriveJsonDecoder.gen[Hits]
}

private[elasticsearch] final case class Total(value: Long, relation: String)

private[elasticsearch] object Total {
  implicit val decoder: JsonDecoder[Total] = DeriveJsonDecoder.gen[Total]
}

private[elasticsearch] final case class Item(
  @jsonField("_index")
  index: String,
  @jsonField("_type")
  `type`: String,
  @jsonField("_id")
  id: String,
  @jsonField("_score")
  score: Double,
  @jsonField("_source")
  source: Json
)

private[elasticsearch] object Item {
  implicit val decoder: JsonDecoder[Item] = DeriveJsonDecoder.gen[Item]
}
