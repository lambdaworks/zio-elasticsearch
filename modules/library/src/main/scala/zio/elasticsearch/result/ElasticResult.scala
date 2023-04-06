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

import zio.elasticsearch.executor.response.AggregationResponse
import zio.json.ast.Json
import zio.prelude.ZValidation
import zio.schema.Schema
import zio.{IO, Task, UIO, ZIO}

private[elasticsearch] sealed trait AggregationsResult {
  def aggregation(name: String): Task[Option[AggregationResponse]]

  def aggregations: Task[Map[String, AggregationResponse]]
}

private[elasticsearch] sealed trait DocumentResult[F[_]] {
  def documentAs[A: Schema]: Task[F[A]]
}

final class AggregationResult private[elasticsearch] (
  private val aggs: Map[String, AggregationResponse]
) extends AggregationsResult {
  def aggregation(name: String): Task[Option[AggregationResponse]] =
    ZIO.succeed(aggs.get(name))

  def aggregations: Task[Map[String, AggregationResponse]] =
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
      .mapError(e => DecodingException(s"Could not parse the document: ${e.message}"))
}

final class SearchResult private[elasticsearch] (private val hits: List[Item], private val lastSort: Option[Json])
    extends DocumentResult[List] {
  def documentAs[A: Schema]: IO[DecodingException, List[A]] =
    ZIO.fromEither {
      ZValidation.validateAll(hits.map(item => ZValidation.fromEither(item.documentAs))).toEitherWith { errors =>
        DecodingException(s"Could not parse all documents successfully: ${errors.map(_.message).mkString(",")})")
      }
    }

  lazy val items: UIO[List[Item]] = ZIO.succeed(hits)

  lazy val lastSortValue: UIO[Option[Json]] = ZIO.succeed(lastSort)
}

final class SearchAndAggregateResult private[elasticsearch] (
  private val hits: List[Item],
  private val aggs: Map[String, AggregationResponse]
) extends DocumentResult[List]
    with AggregationsResult {
  def aggregation(name: String): Task[Option[AggregationResponse]] =
    ZIO.succeed(aggs.get(name))

  def aggregations: Task[Map[String, AggregationResponse]] =
    ZIO.succeed(aggs)

  def documentAs[A: Schema]: Task[List[A]] =
    ZIO.fromEither {
      ZValidation.validateAll(hits.map(item => ZValidation.fromEither(item.documentAs))).toEitherWith { errors =>
        DecodingException(
          s"Could not parse all documents successfully: ${errors.map(_.message).mkString(",")})"
        )
      }
    }
}
