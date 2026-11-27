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

import scala.reflect.ClassTag

private[elasticsearch] sealed trait ResultWithAggregation extends AggregationAccessors[Any, DecodingException] {
  def aggregation(name: String): Task[Option[AggregationResult]]

  def aggregationAs[A <: AggregationResult: ClassTag](name: String): IO[DecodingException, Option[A]] =
    aggregation(name)
      .mapBoth(
        e => DecodingException(s"Something went wrong decoding the aggregation with name $name: $e"),
        {
          case Some(agg: A) => Right(Some(agg))
          case Some(_)      => Left(DecodingException(s"Aggregation with name $name was not of type you provided."))
          case None         => Right(None)
        }
      )
      .absolve

  def aggregations: Task[Map[String, AggregationResult]]
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

final class KNNSearchResult private[elasticsearch] (private val hits: Chunk[Item]) extends DocumentResult[Chunk] {

  def documentAs[A: Schema]: IO[DecodingException, Chunk[A]] =
    ZIO.fromEither {
      ZValidation.validateAll(hits.map(item => ZValidation.fromEither(item.documentAs))).toEitherWith { errors =>
        DecodingException(s"Could not parse all documents successfully: ${errors.map(_.message).mkString(", ")}")
      }
    }

  lazy val items: UIO[Chunk[Item]] = ZIO.succeed(hits)
}

final class MultiGetResult private[elasticsearch] (private val docs: Chunk[MultiGetItem])
    extends DocumentResult[MultiGetResult.Documents] {

  /**
   * Decodes the requested documents as type `A`, keeping the order in which they were requested.
   *
   * @tparam A
   *   the type to decode the documents to. An implicit `Schema` instance must be in scope for this type
   * @return
   *   an effect that produces one element per requested document: `Some` with the decoded document if it was found, or
   *   `None` if it was not found. It fails with an [[ElasticException]] if any document could not be retrieved (see
   *   [[failures]]), or with a [[DecodingException]] if any found document could not be decoded or has no `_source`.
   */
  def documentAs[A: Schema]: IO[ElasticException, Chunk[Option[A]]] =
    docs.collect { case failed: MultiGetItem.Failed => failed } match {
      case Chunk() =>
        ZIO.fromEither {
          ZValidation
            .validateAll(docs.map {
              case MultiGetItem.Found(_, _, Some(item)) =>
                ZValidation.fromEither(item.documentAs[A]).map(Option(_)).mapError(_.message)
              case MultiGetItem.Found(index, id, None) =>
                ZValidation.fail(s"Document [$index/$id] was found, but its _source is not available")
              case _ =>
                ZValidation.succeed(None)
            })
            .toEitherWith(errors =>
              DecodingException(s"Could not parse all documents successfully: ${errors.mkString(", ")}")
            )
        }
      case failures =>
        ZIO.fail(
          new ElasticException(
            s"Could not retrieve all documents: ${failures
                .map(f => s"[${f.index}/${f.id}] ${f.error.`type`}: ${f.error.reason}")
                .mkString(", ")}"
          )
        )
    }

  lazy val failures: UIO[Chunk[MultiGetItem.Failed]] =
    ZIO.succeed(docs.collect { case failed: MultiGetItem.Failed => failed })

  lazy val items: UIO[Chunk[MultiGetItem]] = ZIO.succeed(docs)

  lazy val missing: UIO[Chunk[MultiGetItem.NotFound]] =
    ZIO.succeed(docs.collect { case notFound: MultiGetItem.NotFound => notFound })
}

object MultiGetResult {
  type Documents[A] = Chunk[Option[A]]
}

final class MultiSearchResult private[elasticsearch] (private val responses: Chunk[MultiSearchItem])
    extends DocumentResult[MultiSearchResult.Documents] {

  /**
   * Decodes the documents found by each search as type `A`, keeping the order in which the searches were requested.
   *
   * @tparam A
   *   the type to decode the documents to. An implicit `Schema` instance must be in scope for this type
   * @return
   *   an effect that produces one element per requested search, containing the documents it found. It fails with an
   *   [[ElasticException]] if any search failed or its response could not be parsed (see [[items]]), or with a
   *   [[DecodingException]] listing, by search position, every document that could not be decoded.
   */
  def documentAs[A: Schema]: IO[ElasticException, Chunk[Chunk[A]]] =
    results.flatMap { results =>
      ZIO.fromEither {
        ZValidation
          .validateAll(results.zipWithIndex.map { case (result, position) =>
            ZValidation.validateAll(
              result.hits
                .map(item => ZValidation.fromEither(item.documentAs[A]).mapError(e => s"[$position] ${e.message}"))
            )
          })
          .toEitherWith(errors =>
            DecodingException(s"Could not parse all documents successfully: ${errors.mkString(", ")}")
          )
      }
    }

  /**
   * Returns the searches that Elasticsearch reported as failed. It does not include searches whose response could not
   * be parsed (see [[undecodable]]).
   */
  lazy val failures: UIO[Chunk[MultiSearchItem.Failed]] =
    ZIO.succeed(responses.collect { case failed: MultiSearchItem.Failed => failed })

  lazy val items: UIO[Chunk[MultiSearchItem]] = ZIO.succeed(responses)

  /**
   * Returns the searches whose response could not be parsed. Together with [[failures]], it contains every search that
   * makes [[results]] and [[documentAs]] fail.
   */
  lazy val undecodable: UIO[Chunk[MultiSearchItem.Undecodable]] =
    ZIO.succeed(responses.collect { case undecodable: MultiSearchItem.Undecodable => undecodable })

  /**
   * Returns the result of each search, keeping the order in which the searches were requested.
   *
   * @return
   *   an effect that produces one [[SearchAndAggregateResult]] per requested search. It fails with an
   *   [[ElasticException]] if any search failed (see [[failures]]) or its response could not be parsed (see
   *   [[undecodable]]).
   */
  lazy val results: IO[ElasticException, Chunk[SearchAndAggregateResult]] =
    responses.zipWithIndex.collect {
      case (MultiSearchItem.Failed(_, error), position) if error.reason.isEmpty =>
        s"[$position] ${error.`type`}"
      case (MultiSearchItem.Failed(_, error), position) =>
        s"[$position] ${error.`type`}: ${error.reason}"
      case (MultiSearchItem.Undecodable(_, _, message), position) =>
        s"[$position] could not parse the response: $message"
    } match {
      case Chunk() =>
        ZIO.succeed(responses.collect { case MultiSearchItem.Succeeded(_, result) => result })
      case failures =>
        ZIO.fail(new ElasticException(s"Could not execute all searches: ${failures.mkString(", ")}"))
    }
}

object MultiSearchResult {
  type Documents[A] = Chunk[Chunk[A]]
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
      .mapBoth(_ => new ElasticException("Total hits are not being tracked."), _.value)
}

final class SearchAndAggregateResult private[elasticsearch] (
  private[elasticsearch] val hits: Chunk[Item],
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
      .mapBoth(_ => new ElasticException("Total hits are not being tracked."), _.value)
}
