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

package zio.elasticsearch.executor.response

import zio.json.ast.Json
import zio.json.ast.Json.Obj
import zio.json.{DeriveJsonDecoder, JsonDecoder, jsonField}
import zio.prelude.{Validation, ZValidation}

private[elasticsearch] final case class SearchWithAggregationsResponse(
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

  lazy val resultsWithHighlights: List[(Json, Option[Json])] = hits.hits.map(h => (h.source, h.highlight))

  lazy val lastSortField: Option[Json] = hits.hits.lastOption.flatMap(_.sort)

  def aggs: Map[String, AggregationResponse] =
    aggregations.fold[Map[String, AggregationResponse]](
      Map.empty[String, AggregationResponse]
    )(aggregations =>
      (Obj.decoder.decodeJson(aggregations.toString): @unchecked) match {
        case Right(res) =>
          (Validation
            .validateAll(
              res.fields.toList.map { case (field, data) =>
                ZValidation.fromEither(
                  (field: @unchecked) match {
                    case str if str.contains("terms#") =>
                      TermsAggregationResponse.decoder
                        .decodeJson(data.toString)
                        .map(field.split("#")(1) -> _)
                  }
                )
              }
            ): @unchecked) match {
            case ZValidation.Success(_, value) => value.toMap
          }
      }
    )
}

private[elasticsearch] object SearchWithAggregationsResponse {
  implicit val decoder: JsonDecoder[SearchWithAggregationsResponse] =
    DeriveJsonDecoder.gen[SearchWithAggregationsResponse]
}
