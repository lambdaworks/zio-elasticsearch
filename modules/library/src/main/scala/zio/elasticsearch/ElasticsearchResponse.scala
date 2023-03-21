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
import zio.json.ast.Json.Obj
import zio.json.{DeriveJsonDecoder, JsonDecoder, jsonField}
import zio.prelude.{Validation, ZValidation}

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

  def aggs: Map[String, ElasticAggregationResponse] =
    aggregations.fold[Map[String, ElasticAggregationResponse]](
      Map.empty[String, ElasticAggregationResponse]
    )(aggregations =>
      (Obj.decoder.decodeJson(aggregations.toString): @unchecked) match {
        case Right(res) =>
          (Validation
            .validateAll(
              res.fields.toList.map { case (field, data) =>
                ZValidation.fromEither(
                  field match {
                    case str if str.contains("terms#") =>
                      TermsAggregationResponse.decoder
                        .decodeJson(data.toString)
                        .map(field.split("#")(1) -> _)
                    case _ =>
                      Left(DecodingException("Could not parse all aggregations successfully."))
                  }
                )
              }
            ): @unchecked) match {
            case ZValidation.Success(_, value) => value.toMap
          }
      }
    )
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

private[elasticsearch] final case class Total(value: Long, relation: String)

private[elasticsearch] object Total {
  implicit val decoder: JsonDecoder[Total] = DeriveJsonDecoder.gen[Total]
}

sealed trait ElasticAggregationResponse

final case class TermsAggregationResponse(
  @jsonField("doc_count_error_upper_bound")
  docErrorCount: Int,
  @jsonField("sum_other_doc_count")
  sumOtherDocCount: Int,
  buckets: List[TermsAggregationBucket]
) extends ElasticAggregationResponse

object TermsAggregationResponse {
  implicit val decoder: JsonDecoder[TermsAggregationResponse] = DeriveJsonDecoder.gen[TermsAggregationResponse]
}

sealed trait ElasticAggregationBucket

final case class TermsAggregationBucket(
  key: String,
  @jsonField("doc_count")
  docCount: Int,
  subAggregations: Option[Map[String, ElasticAggregationResponse]] = None
) extends ElasticAggregationBucket

object TermsAggregationBucket {
  implicit val decoder: JsonDecoder[TermsAggregationBucket] = Obj.decoder.mapOrFail { case Obj(fields) =>
    val allFields = fields.flatMap { case (field, data) =>
      field match {
        case "key" =>
          Some(field -> data.toString.replaceAll("\"", ""))
        case "doc_count" =>
          Some(field -> data.unsafeAs[Int])
        case _ =>
          val objFields = data.unsafeAs[Obj].fields.toMap

          (field: @unchecked) match {
            case str if str.contains("terms#") =>
              Some(
                field -> TermsAggregationResponse(
                  docErrorCount = objFields("doc_count_error_upper_bound").unsafeAs[Int],
                  sumOtherDocCount = objFields("sum_other_doc_count").unsafeAs[Int],
                  buckets = objFields("buckets")
                    .unsafeAs[List[Json]]
                    .map(_.unsafeAs[TermsAggregationBucket](TermsAggregationBucket.decoder))
                )
              )
          }
      }
    }.toMap

    val key      = allFields("key").asInstanceOf[String]
    val docCount = allFields("doc_count").asInstanceOf[Int]
    val subAggs = allFields.collect {
      case (field, data) if field != "key" && field != "doc_count" =>
        field match {
          case str if str.contains("terms#") =>
            (field.split("#")(1), data.asInstanceOf[TermsAggregationResponse])
        }
    }

    Right(TermsAggregationBucket.apply(key, docCount, Option(subAggs).filter(_.nonEmpty)))
  }

  final implicit class JsonDecoderOps(json: Json) {
    def unsafeAs[A](implicit decoder: JsonDecoder[A]): A =
      (json.as[A]: @unchecked) match {
        case Right(decoded) => decoded
      }
  }
}
