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

import zio.elasticsearch.ElasticResult.decodeAggregationsJson
import zio.json.ast.Json
import zio.json.ast.Json.Obj
import zio.prelude.{Validation, ZValidation}
import zio.schema.Schema
import zio.{IO, Task, ZIO}

sealed trait AggregationsResult {
  def aggregation(name: String): Task[Option[ElasticAggregationResponse]]

  def aggregations: Task[Map[String, ElasticAggregationResponse]]
}

sealed trait DocumentResult[F[_]] {
  def documentAs[A: Schema]: Task[F[A]]
}

final class AggregationResult private[elasticsearch] (private val aggregationsJson: Option[Json])
    extends AggregationsResult {
  def aggregation(name: String): Task[Option[ElasticAggregationResponse]] = ZIO.fromEither {
    decodeAggregationsJson(aggregationsJson, Some(name)).map(_.get(name))
  }

  def aggregations: Task[Map[String, ElasticAggregationResponse]] = ZIO.fromEither {
    decodeAggregationsJson(aggregationsJson, None)
  }
}

final class GetResult private[elasticsearch] (private val doc: Option[Item]) extends DocumentResult[Option] {
  def documentAs[A: Schema]: IO[DecodingException, Option[A]] =
    ZIO
      .fromEither(doc match {
        case Some(item) =>
          item.documentAs match {
            case Right(doc) => Right(Some(doc))
            case Left(e)    => Left(DecodingException(s"Could not parse the document: ${e.message}"))
          }
        case None =>
          Right(None)
      })
      .mapError(e => DecodingException(s"Could not parse the document: ${e.message}"))
}

final class SearchResult private[elasticsearch] (private val hits: List[Item]) extends DocumentResult[List] {
  def documentAs[A: Schema]: IO[DecodingException, List[A]] =
    ZIO.fromEither {
      ZValidation.validateAll(hits.map(item => ZValidation.fromEither(item.documentAs))).toEitherWith { errors =>
        DecodingException(s"Could not parse all documents successfully: ${errors.map(_.message).mkString(",")})")
      }
    }
}

final class SearchWithAggregationsResult private[elasticsearch] (
  private val hits: List[Item],
  private val aggregationsJson: Option[Json]
) extends DocumentResult[List]
    with AggregationsResult {
  def aggregation(name: String): Task[Option[ElasticAggregationResponse]] = ZIO.fromEither {
    decodeAggregationsJson(aggregationsJson, Some(name)).map(_.get(name))
  }

  def aggregations: Task[Map[String, ElasticAggregationResponse]] = ZIO.fromEither {
    decodeAggregationsJson(aggregationsJson, None)
  }

  def documentAs[A: Schema]: Task[List[A]] = ZIO.fromEither {
    ZValidation.validateAll(hits.map(item => ZValidation.fromEither(item.documentAs))).toEitherWith { errors =>
      DecodingException(
        s"Could not parse all documents successfully: ${errors.map(_.message).mkString(",")})"
      )
    }
  }
}

private object ElasticResult {
  def decodeAggregationsJson(
    aggregationsJson: Option[Json],
    name: Option[String]
  ): Either[DecodingException, Map[String, TermsAggregationResponse]] = {

    val decodingExceptionMessage = name match {
      case Some(str) => s"Could not parse aggregation $str successfully."
      case None      => "Could not parse all aggregations successfully."
    }

    def getUsefulPairs(pairs: List[(String, Json)]): List[(String, Json)] =
      name match {
        case Some(str) => pairs.filter { case (field, _) => field == str }
        case None      => pairs
      }

    aggregationsJson.fold[Either[DecodingException, Map[String, TermsAggregationResponse]]](
      Right(Map.empty[String, TermsAggregationResponse])
    )(aggregations =>
      Obj.decoder.decodeJson(aggregations.toString) match {
        case Right(res) =>
          Validation
            .validateAll(
              getUsefulPairs(res.fields.toList).map { case (field, data) =>
                ZValidation.fromEither(
                  field match {
                    case str if str.contains("terms#") =>
                      TermsAggregationResponse.decoder
                        .decodeJson(data.toString)
                        .map(field.split("#")(1) -> _)
                    case _ =>
                      Left(
                        DecodingException(
                          s"$decodingExceptionMessage."
                        )
                      )
                  }
                )
              }
            )
            .toEitherWith { errors =>
              DecodingException(
                s"$decodingExceptionMessage: ${errors.mkString(",")})"
              )
            }
            .map(_.toMap)
        case Left(e) =>
          Left(DecodingException(s"$decodingExceptionMessage: $e)"))
      }
    )
  }
}
