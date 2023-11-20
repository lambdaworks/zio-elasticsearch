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

package zio.elasticsearch.result

import zio.elasticsearch.executor.response.SearchWithAggregationsResponse
import zio.json.ast.Json
import zio.prelude.ZValidation
import zio.schema.Schema
import zio.{Chunk, IO, Task, UIO, ZIO}

import scala.util.{Failure, Success, Try}

private[elasticsearch] sealed trait ResultWithAggregation {
  def aggregation(name: String): Task[Option[AggregationResult]]

  def aggregationAs[A <: AggregationResult](name: String): IO[DecodingException, Option[A]] =
    aggregation(name)
      .mapError(e => DecodingException(s"Something went wrong decoding the aggregation with name $name: $e"))
      .map {
        case Some(aggRes) =>
          Try(aggRes.asInstanceOf[A]) match {
            case Failure(_)   => Left(DecodingException(s"Aggregation with name $name was not of type you provided."))
            case Success(agg) => Right(Some(agg))
          }
        case None => Right(None)
      }
      .absolve

  def aggregations: Task[Map[String, AggregationResult]]

  def asAvgAggregation(name: String): IO[DecodingException, Option[AvgAggregationResult]] =
    aggregationAs[AvgAggregationResult](name)

  def asCardinalityAggregation(name: String): IO[DecodingException, Option[CardinalityAggregationResult]] =
    aggregationAs[CardinalityAggregationResult](name)

  def asExtendedStatsAggregation(name: String): IO[DecodingException, Option[ExtendedStatsAggregationResult]] =
    aggregationAs[ExtendedStatsAggregationResult](name)

  def asMaxAggregation(name: String): IO[DecodingException, Option[MaxAggregationResult]] =
    aggregationAs[MaxAggregationResult](name)

  def asMinAggregation(name: String): IO[DecodingException, Option[MinAggregationResult]] =
    aggregationAs[MinAggregationResult](name)

  def asPercentileRanksAggregation(name: String): IO[DecodingException, Option[PercentileRanksAggregationResult]] =
    aggregationAs[PercentileRanksAggregationResult](name)

  def asPercentilesAggregation(name: String): IO[DecodingException, Option[PercentilesAggregationResult]] =
    aggregationAs[PercentilesAggregationResult](name)

  def asSumAggregation(name: String): IO[DecodingException, Option[SumAggregationResult]] =
    aggregationAs[SumAggregationResult](name)

  def asTermsAggregation(name: String): IO[DecodingException, Option[TermsAggregationResult]] =
    aggregationAs[TermsAggregationResult](name)

  def asValueCountAggregation(name: String): IO[DecodingException, Option[ValueCountAggregationResult]] =
    aggregationAs[ValueCountAggregationResult](name)

  def asWeightedAvgAggregation(name: String): IO[DecodingException, Option[WeightedAvgAggregationResult]] =
    aggregationAs[WeightedAvgAggregationResult](name)
}

private[elasticsearch] sealed trait DocumentResult[F[_]] {
  def documentAs[A: Schema]: Task[F[A]]
}

final class AggregateResult private[elasticsearch] (
  private val aggs: Map[String, AggregationResult]
) extends ResultWithAggregation {

  def aggregation(name: String): Task[Option[AggregationResult]] =
    ZIO.succeed(aggs.get(name))

  def aggregations: Task[Map[String, AggregationResult]] =
    ZIO.succeed(aggs)
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
}

final class SearchResult private[elasticsearch] (
  private val hits: Chunk[Item],
  private val fullResponse: SearchWithAggregationsResponse
) extends DocumentResult[Chunk] {

  def documentAs[A: Schema]: IO[DecodingException, Chunk[A]] =
    ZIO.fromEither {
      ZValidation.validateAll(hits.map(item => ZValidation.fromEither(item.documentAs))).toEitherWith { errors =>
        DecodingException(s"Could not parse all documents successfully: ${errors.map(_.message).mkString(", ")}")
      }
    }

  lazy val items: UIO[Chunk[Item]] = ZIO.succeed(hits)

  lazy val lastSortValue: UIO[Option[Json]] = ZIO.succeed(fullResponse.lastSortField)

  lazy val response: UIO[SearchWithAggregationsResponse] = ZIO.succeed(fullResponse)

  lazy val total: IO[ElasticException, Long] =
    ZIO
      .fromOption(fullResponse.hits.total)
      .map(_.value)
      .mapError(_ => new ElasticException("Total hits are not being tracked."))
}

final class SearchAndAggregateResult private[elasticsearch] (
  private val hits: Chunk[Item],
  private val aggs: Map[String, AggregationResult],
  private val fullResponse: SearchWithAggregationsResponse
) extends DocumentResult[Chunk]
    with ResultWithAggregation {

  def aggregation(name: String): Task[Option[AggregationResult]] =
    ZIO.succeed(aggs.get(name))

  def aggregations: Task[Map[String, AggregationResult]] =
    ZIO.succeed(aggs)

  def documentAs[A: Schema]: Task[Chunk[A]] =
    ZIO.fromEither {
      ZValidation.validateAll(hits.map(item => ZValidation.fromEither(item.documentAs))).toEitherWith { errors =>
        DecodingException(
          s"Could not parse all documents successfully: ${errors.map(_.message).mkString(",")})"
        )
      }
    }

  lazy val items: UIO[Chunk[Item]] = ZIO.succeed(hits)

  lazy val lastSortValue: UIO[Option[Json]] = ZIO.succeed(fullResponse.lastSortField)

  lazy val response: UIO[SearchWithAggregationsResponse] = ZIO.succeed(fullResponse)

  lazy val total: IO[ElasticException, Long] =
    ZIO
      .fromOption(fullResponse.hits.total)
      .map(_.value)
      .mapError(_ => new ElasticException("Total hits are not being tracked."))
}
