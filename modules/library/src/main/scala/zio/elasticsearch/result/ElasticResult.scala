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

import zio.elasticsearch.executor.response.ElasticAggregationResponse
import zio.prelude.ZValidation
import zio.schema.Schema
import zio.{IO, Task, ZIO}

private[elasticsearch] sealed trait AggregationsResult {
  def aggregation(name: String): Task[Option[ElasticAggregationResponse]]

  def aggregations: Task[Map[String, ElasticAggregationResponse]]
}

private[elasticsearch] sealed trait DocumentResult[F[_]] {
  def documentAs[A: Schema]: Task[F[A]]
}

private[elasticsearch] final class AggregationResult private[elasticsearch] (
  private val aggs: Map[String, ElasticAggregationResponse]
) extends AggregationsResult {
  def aggregation(name: String): Task[Option[ElasticAggregationResponse]] =
    ZIO.succeed(aggs.get(name))

  def aggregations: Task[Map[String, ElasticAggregationResponse]] =
    ZIO.succeed(aggs)
}

private[elasticsearch] final class GetResult private[elasticsearch] (private val doc: Option[Item])
    extends DocumentResult[Option] {
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

private[elasticsearch] final class SearchResult private[elasticsearch] (private val hits: List[Item])
    extends DocumentResult[List] {
  def documentAs[A: Schema]: IO[DecodingException, List[A]] =
    ZIO.fromEither {
      ZValidation.validateAll(hits.map(item => ZValidation.fromEither(item.documentAs))).toEitherWith { errors =>
        DecodingException(s"Could not parse all documents successfully: ${errors.map(_.message).mkString(",")})")
      }
    }
}

private[elasticsearch] final class SearchAndAggregateResult private[elasticsearch] (
  private val hits: List[Item],
  private val aggs: Map[String, ElasticAggregationResponse]
) extends DocumentResult[List]
    with AggregationsResult {
  def aggregation(name: String): Task[Option[ElasticAggregationResponse]] =
    ZIO.succeed(aggs.get(name))

  def aggregations: Task[Map[String, ElasticAggregationResponse]] =
    ZIO.succeed(aggs)

  def documentAs[A: Schema]: Task[List[A]] = ZIO.fromEither {
    ZValidation.validateAll(hits.map(item => ZValidation.fromEither(item.documentAs))).toEitherWith { errors =>
      DecodingException(
        s"Could not parse all documents successfully: ${errors.map(_.message).mkString(",")})"
      )
    }
  }
}
