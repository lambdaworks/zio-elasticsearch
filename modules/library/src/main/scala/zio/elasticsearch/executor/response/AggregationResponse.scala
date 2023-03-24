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

sealed trait AggregationResponse

private[elasticsearch] final case class TermsAggregationResponse(
  @jsonField("doc_count_error_upper_bound")
  docErrorCount: Int,
  @jsonField("sum_other_doc_count")
  sumOtherDocCount: Int,
  buckets: List[TermsAggregationBucket]
) extends AggregationResponse

private[elasticsearch] object TermsAggregationResponse {
  implicit val decoder: JsonDecoder[TermsAggregationResponse] = DeriveJsonDecoder.gen[TermsAggregationResponse]
}

private[elasticsearch] sealed trait AggregationBucket

private[elasticsearch] final case class TermsAggregationBucket(
  key: String,
  @jsonField("doc_count")
  docCount: Int,
  subAggregations: Option[Map[String, AggregationResponse]] = None
) extends AggregationBucket

private[elasticsearch] object TermsAggregationBucket {
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
