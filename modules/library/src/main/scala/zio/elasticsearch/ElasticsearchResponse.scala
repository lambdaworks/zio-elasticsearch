/*
 * Copyright 2022 LambdaWorks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package zio.elasticsearch

import zio.json.ast.Json
import zio.json.{DeriveJsonDecoder, JsonDecoder, jsonField}

private[elasticsearch] final case class ElasticCountResponse(
  count: Int,
  @jsonField("_shards")
  shards: Shards
)

private[elasticsearch] object ElasticCountResponse {
  implicit val decoder: JsonDecoder[ElasticCountResponse] = DeriveJsonDecoder.gen[ElasticCountResponse]
}

private[elasticsearch] final case class ElasticCreateResponse(
  @jsonField("_id")
  id: String
)

private[elasticsearch] object ElasticCreateResponse {
  implicit val decoder: JsonDecoder[ElasticCreateResponse] = DeriveJsonDecoder.gen[ElasticCreateResponse]
}

private[elasticsearch] final case class ElasticGetResponse(
  @jsonField("_source")
  source: Json
)

private[elasticsearch] object ElasticGetResponse {
  implicit val decoder: JsonDecoder[ElasticGetResponse] = DeriveJsonDecoder.gen[ElasticGetResponse]
}

private[elasticsearch] final case class ElasticSearchAndAggsResponse(
  @jsonField("pit_id")
  pitId: Option[String],
  @jsonField("_scroll_id")
  scrollId: Option[String],
  took: Int,
  @jsonField("timed_out")
  timedOut: Boolean,
  @jsonField("_shards")
  shards: Shards,
  hits: Hits,
  aggregations: Option[Json]
) {

  lazy val results: List[Json] = hits.hits.map(_.source)

  lazy val lastSortField: Option[Json] = hits.hits.lastOption.flatMap(_.sort)
}

private[elasticsearch] object ElasticSearchAndAggsResponse {
  implicit val decoder: JsonDecoder[ElasticSearchAndAggsResponse] = DeriveJsonDecoder.gen[ElasticSearchAndAggsResponse]
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
  maxScore: Option[Double] = None,
  hits: List[Hit]
)

private[elasticsearch] object Hits {
  implicit val decoder: JsonDecoder[Hits] = DeriveJsonDecoder.gen[Hits]
}

private[elasticsearch] final case class Total(value: Long, relation: String)

private[elasticsearch] object Total {
  implicit val decoder: JsonDecoder[Total] = DeriveJsonDecoder.gen[Total]
}

private[elasticsearch] final case class Hit(
  @jsonField("_index")
  index: String,
  @jsonField("_type")
  `type`: String,
  @jsonField("_id")
  id: String,
  @jsonField("_score")
  score: Option[Double] = None,
  @jsonField("_source")
  source: Json,
  sort: Option[Json]
)

private[elasticsearch] object Hit {
  implicit val decoder: JsonDecoder[Hit] = DeriveJsonDecoder.gen[Hit]
}
