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

private[elasticsearch] final case class ElasticQueryResponse(
  @jsonField("_scroll_id")
  scrollId: Option[String],
  took: Int,
  @jsonField("timed_out")
  timedOut: Boolean,
  @jsonField("_shards")
  shards: Shards,
  hits: Hits
) {

  lazy val results: List[Json] = hits.hits.map(_.source)
}

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
  score: Double,
  @jsonField("_source")
  source: Json
)

private[elasticsearch] object Hit {
  implicit val decoder: JsonDecoder[Hit] = DeriveJsonDecoder.gen[Hit]
}
