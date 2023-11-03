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

package zio.elasticsearch.executor

import sttp.client3.ziojson._
import sttp.client3.{Identity, RequestT, Response, ResponseException, SttpBackend, UriContext, basicRequest => request}
import sttp.model.MediaType.ApplicationJson
import sttp.model.StatusCode.{
  BadRequest => HttpBadRequest,
  Conflict => HttpConflict,
  Created => HttpCreated,
  Forbidden => HttpForbidden,
  NotFound => HttpNotFound,
  Ok => HttpOk,
  Unauthorized => HttpUnauthorized
}
import sttp.model.Uri.QuerySegment
import zio.ZIO.logDebug
import zio.elasticsearch.ElasticPrimitive.ElasticPrimitiveOps
import zio.elasticsearch.ElasticRequest._
import zio.elasticsearch._
import zio.elasticsearch.executor.response.AggregationResponse.toResult
import zio.elasticsearch.executor.response.{
  BulkResponse,
  CountResponse,
  CreateResponse,
  DocumentWithHighlightsAndSort,
  GetResponse,
  Hit,
  SearchWithAggregationsResponse,
  UpdateByQueryResponse
}
import zio.elasticsearch.request.{CreationOutcome, DeletionOutcome, UpdateOutcome}
import zio.elasticsearch.result._
import zio.json.ast.Json
import zio.json.ast.Json.{Arr, Obj}
import zio.json.{DeriveJsonDecoder, JsonDecoder}
import zio.schema.Schema
import zio.stream.{Stream, ZStream}
import zio.{Chunk, Task, ZIO}

import scala.collection.immutable.{Map => ScalaMap}

private[elasticsearch] final class HttpExecutor private (esConfig: ElasticConfig, client: SttpBackend[Task, Any])
    extends Executor {

  import HttpExecutor._

  private val baseRequest = esConfig.credentials match {
    case Some(credentials) => request.auth.basic(credentials.username, credentials.password)
    case _                 => request
  }

  def execute[A](request: ElasticRequest[A]): Task[A] =
    request match {
      case r: Aggregate          => executeAggregate(r)
      case r: Bulk               => executeBulk(r)
      case r: Count              => executeCount(r)
      case r: Create             => executeCreate(r)
      case r: CreateWithId       => executeCreateWithId(r)
      case r: CreateIndex        => executeCreateIndex(r)
      case r: CreateOrUpdate     => executeCreateOrUpdate(r)
      case r: DeleteById         => executeDeleteById(r)
      case r: DeleteByQuery      => executeDeleteByQuery(r)
      case r: DeleteIndex        => executeDeleteIndex(r)
      case r: Exists             => executeExists(r)
      case r: GetById            => executeGetById(r)
      case r: Refresh            => executeRefresh(r)
      case r: Search             => executeSearch(r)
      case r: SearchAndAggregate => executeSearchAndAggregate(r)
      case r: Update             => executeUpdate(r)
      case r: UpdateByQuery      => executeUpdateByQuery(r)
    }

  def stream(r: SearchRequest): Stream[Throwable, Item] =
    stream(r, StreamConfig.Default)

  def stream(request: SearchRequest, config: StreamConfig): Stream[Throwable, Item] =
    request match {
      case r: Search =>
        if (config.searchAfter) {
          ZStream.paginateChunkZIO[Any, Throwable, Item, (String, Option[Json])](("", None)) {
            case ("", _) =>
              executeCreatePointInTime(r.selectors, config)
            case (pitId, searchAfter) =>
              executeSearchAfterRequest(r = r, pitId = pitId, config = config, searchAfter = searchAfter)
          }
        } else {
          ZStream.paginateChunkZIO("") { s =>
            if (s.isEmpty) executeSearchWithScroll(r, config) else executeGetByScroll(s, config)
          }
        }
    }

  def streamAs[A: Schema](request: SearchRequest): Stream[Throwable, A] =
    stream(request).map(_.documentAs[A]).collectWhileRight

  def streamAs[A: Schema](r: SearchRequest, config: StreamConfig): Stream[Throwable, A] =
    stream(r, config).map(_.documentAs[A]).collectWhileRight

  private def executeAggregate(r: Aggregate): Task[AggregateResult] =
    sendRequestWithCustomResponse(
      baseRequest
        .post(uri"${esConfig.uri}/${r.selectors}/$Search?typed_keys")
        .response(asJson[SearchWithAggregationsResponse])
        .contentType(ApplicationJson)
        .body(r.toJson)
    ).flatMap { response =>
      response.code match {
        case HttpOk =>
          response.body.fold(
            e => ZIO.fail(new ElasticException(s"Exception occurred: ${e.getMessage}")),
            value =>
              ZIO.succeed(new AggregateResult(value.aggs.map { case (key, response) =>
                (key, toResult(response))
              }))
          )
        case _ =>
          ZIO.fail(handleFailuresFromCustomResponse(response))
      }
    }

  private def executeBulk(r: Bulk): Task[BulkResponse] = {
    val uri = (r.index match {
      case Some(index) => uri"${esConfig.uri}/$index/$Bulk"
      case None        => uri"${esConfig.uri}/$Bulk"
    }).withParams(getQueryParams(Chunk(("refresh", r.refresh), ("routing", r.routing))))

    sendRequestWithCustomResponse(
      baseRequest
        .post(uri)
        .contentType(ApplicationJson)
        .body(r.body)
        .response(asJson[BulkResponse])
    ).flatMap { response =>
      response.code match {
        case HttpOk =>
          response.body.fold(
            e => ZIO.fail(new ElasticException(s"Exception occurred: ${e.getMessage}")),
            value => ZIO.succeed(value)
          )
        case _ => ZIO.fail(handleFailuresFromCustomResponse(response))
      }
    }
  }

  private def executeCount(r: Count): Task[Int] = {
    val req = baseRequest
      .get(uri"${esConfig.uri}/${r.index}/$Count".withParams(getQueryParams(Chunk(("routing", r.routing)))))
      .contentType(ApplicationJson)
      .response(asJson[CountResponse])

    sendRequestWithCustomResponse(r.query.fold(req)(_ => req.body(r.toJson))).flatMap { response =>
      response.code match {
        case HttpOk =>
          response.body
            .map(_.count)
            .fold(
              e => ZIO.fail(new ElasticException(s"Exception occurred: ${e.getMessage}")),
              value => ZIO.succeed(value)
            )
        case _ =>
          ZIO.fail(handleFailuresFromCustomResponse(response))
      }
    }
  }

  private def executeCreate(r: Create): Task[DocumentId] = {
    val uri = uri"${esConfig.uri}/${r.index}/$Doc"
      .withParams(getQueryParams(Chunk(("refresh", r.refresh), ("routing", r.routing))))

    sendRequestWithCustomResponse[CreateResponse](
      baseRequest
        .post(uri)
        .contentType(ApplicationJson)
        .body(r.toJson)
        .response(asJson[CreateResponse])
    ).flatMap { response =>
      response.code match {
        case HttpCreated =>
          response.body
            .map(res => DocumentId(res.id))
            .fold(
              e => ZIO.fail(new ElasticException(s"Exception occurred: ${e.getMessage}")),
              value => ZIO.succeed(value)
            )
        case _ =>
          ZIO.fail(handleFailuresFromCustomResponse(response))
      }
    }

  }

  private def executeCreateWithId(r: CreateWithId): Task[CreationOutcome] = {
    val uri = uri"${esConfig.uri}/${r.index}/$Create/${r.id}"
      .withParams(getQueryParams(Chunk(("refresh", r.refresh), ("routing", r.routing))))

    sendRequest(
      baseRequest
        .post(uri)
        .contentType(ApplicationJson)
        .body(r.toJson)
    ).flatMap { response =>
      response.code match {
        case HttpCreated  => ZIO.succeed(CreationOutcome.Created)
        case HttpConflict => ZIO.succeed(CreationOutcome.AlreadyExists)
        case _            => ZIO.fail(handleFailures(response))
      }
    }
  }

  private def executeCreateIndex(r: CreateIndex): Task[CreationOutcome] =
    sendRequest(
      baseRequest
        .put(uri"${esConfig.uri}/${r.index}")
        .contentType(ApplicationJson)
        .body(r.toJson)
    ).flatMap { response =>
      response.code match {
        case HttpOk         => ZIO.succeed(CreationOutcome.Created)
        case HttpBadRequest => ZIO.succeed(CreationOutcome.AlreadyExists)
        case _              => ZIO.fail(handleFailures(response))
      }
    }

  private def executeCreateOrUpdate(r: CreateOrUpdate): Task[Unit] = {
    val uri = uri"${esConfig.uri}/${r.index}/$Doc/${r.id}"
      .withParams(getQueryParams(Chunk(("refresh", r.refresh), ("routing", r.routing))))

    sendRequest(baseRequest.put(uri).contentType(ApplicationJson).body(r.toJson)).flatMap { response =>
      response.code match {
        case HttpOk | HttpCreated => ZIO.unit
        case _                    => ZIO.fail(handleFailures(response))
      }
    }
  }

  private def executeCreatePointInTime(
    index: String,
    config: StreamConfig
  ): Task[(Chunk[Item], Option[(String, Option[Json])])] =
    sendRequestWithCustomResponse(
      baseRequest
        .post(uri"${esConfig.uri}/$index/$PointInTime".withParams((KeepAlive, config.keepAlive)))
        .response(asJson[PointInTimeResponse])
        .contentType(ApplicationJson)
    ).flatMap { response =>
      response.code match {
        case HttpOk =>
          response.body.fold(
            e => ZIO.fail(new ElasticException(s"Exception occurred: ${e.getMessage}")),
            pit => ZIO.succeed((Chunk(), Some((pit.id, None))))
          )
        case _ =>
          ZIO.fail(handleFailuresFromCustomResponse(response))
      }
    }

  private def executeDeleteById(r: DeleteById): Task[DeletionOutcome] = {
    val uri = uri"${esConfig.uri}/${r.index}/$Doc/${r.id}"
      .withParams(getQueryParams(Chunk(("refresh", r.refresh), ("routing", r.routing))))

    sendRequest(baseRequest.delete(uri)).flatMap { response =>
      response.code match {
        case HttpOk       => ZIO.succeed(DeletionOutcome.Deleted)
        case HttpNotFound => ZIO.succeed(DeletionOutcome.NotFound)
        case _            => ZIO.fail(handleFailures(response))
      }
    }
  }

  private def executeDeleteByQuery(r: DeleteByQuery): Task[DeletionOutcome] = {
    val uri =
      uri"${esConfig.uri}/${r.index}/$DeleteByQuery".withParams(
        getQueryParams(Chunk(("refresh", r.refresh), ("routing", r.routing)))
      )

    sendRequest(
      baseRequest
        .post(uri)
        .contentType(ApplicationJson)
        .body(r.toJson)
    ).flatMap { response =>
      response.code match {
        case HttpOk       => ZIO.succeed(DeletionOutcome.Deleted)
        case HttpNotFound => ZIO.succeed(DeletionOutcome.NotFound)
        case _            => ZIO.fail(handleFailures(response))
      }
    }
  }

  private def executeDeleteIndex(r: DeleteIndex): Task[DeletionOutcome] =
    sendRequest(baseRequest.delete(uri"${esConfig.uri}/${r.index}")).flatMap { response =>
      response.code match {
        case HttpOk       => ZIO.succeed(DeletionOutcome.Deleted)
        case HttpNotFound => ZIO.succeed(DeletionOutcome.NotFound)
        case _            => ZIO.fail(handleFailures(response))
      }
    }

  private def executeExists(r: Exists): Task[Boolean] = {
    val uri = uri"${esConfig.uri}/${r.index}/$Doc/${r.id}".withParams(getQueryParams(Chunk(("routing", r.routing))))

    sendRequest(baseRequest.head(uri)).flatMap { response =>
      response.code match {
        case HttpOk       => ZIO.succeed(true)
        case HttpNotFound => ZIO.succeed(false)
        case _            => ZIO.fail(handleFailures(response))
      }
    }
  }

  private def executeGetById(r: GetById): Task[GetResult] = {
    val uri = uri"${esConfig.uri}/${r.index}/$Doc/${r.id}".withParams(
      getQueryParams(Chunk(("refresh", r.refresh), ("routing", r.routing)))
    )

    sendRequestWithCustomResponse[GetResponse](
      baseRequest
        .get(uri)
        .response(asJson[GetResponse])
    ).flatMap { response =>
      response.code match {
        case HttpOk       => ZIO.attempt(new GetResult(doc = response.body.toOption.map(r => Item(r.source))))
        case HttpNotFound => ZIO.succeed(new GetResult(doc = None))
        case _            => ZIO.fail(handleFailuresFromCustomResponse(response))
      }
    }
  }

  private def executeGetByScroll(scrollId: String, config: StreamConfig): Task[(Chunk[Item], Option[String])] =
    sendRequestWithCustomResponse(
      baseRequest
        .post(uri"${esConfig.uri}/$Search/$Scroll".withParams((Scroll, config.keepAlive)))
        .response(asJson[SearchWithAggregationsResponse])
        .contentType(ApplicationJson)
        .body(Obj(ScrollId -> scrollId.toJson))
    ).flatMap { response =>
      response.code match {
        case HttpOk =>
          response.body.fold(
            e => ZIO.fail(new ElasticException(s"Exception occurred: ${e.getMessage}")),
            value =>
              value.resultsWithHighlightsAndSort.toList match {
                case Nil =>
                  ZIO.succeed((Chunk.empty, None))
                case _ =>
                  ZIO.succeed(
                    (
                      itemsFromDocumentsWithHighlights(value.resultsWithHighlightsAndSort),
                      value.scrollId.orElse(Some(scrollId))
                    )
                  )
              }
          )
        case _ =>
          ZIO.fail(handleFailuresFromCustomResponse(response))
      }
    }

  private def executeRefresh(r: Refresh): Task[Boolean] =
    sendRequest(baseRequest.get(uri"${esConfig.uri}/${r.selectors}/$Refresh")).flatMap { response =>
      response.code match {
        case HttpOk       => ZIO.succeed(true)
        case HttpNotFound => ZIO.succeed(false)
        case _            => ZIO.fail(handleFailures(response))
      }
    }

  private def executeSearch(r: Search): Task[SearchResult] =
    sendRequestWithCustomResponse(
      baseRequest
        .post(uri"${esConfig.uri}/${r.selectors}/$Search".withParams(getQueryParams(Chunk(("routing", r.routing)))))
        .response(asJson[SearchWithAggregationsResponse])
        .contentType(ApplicationJson)
        .body(r.toJson)
    ).flatMap { response =>
      response.code match {
        case HttpOk =>
          response.body.fold(
            e => ZIO.fail(new ElasticException(s"Exception occurred: ${e.getMessage}")),
            value => {
              ZIO
                .fromEither(value.innerHitsResults)
                .map { innerHitsResults =>
                  new SearchResult(
                    itemsFromDocumentsWithHighlightsSortAndInnerHits(
                      value.resultsWithHighlightsAndSort,
                      innerHitsResults
                    ),
                    value
                  )
                }
                .mapError(error => DecodingException(s"Could not parse inner_hits: $error"))
            }
          )
        case _ =>
          println(response)
          ZIO.fail(handleFailuresFromCustomResponse(response))
      }
    }

  private def executeSearchAfterRequest(
    r: Search,
    pitId: String,
    config: StreamConfig,
    searchAfter: Option[Json]
  ): Task[(Chunk[Item], Option[(String, Option[Json])])] = {
    val pointInTimeJson =
      Obj(
        "pit" -> Obj(
          "id"      -> pitId.toJson,
          KeepAlive -> config.keepAlive.toJson
        )
      )

    val sortsJson =
      if (r.sortBy.isEmpty) {
        Obj("sort" -> Arr(ShardDoc.toJson), "track_total_hits" -> false.toJson)
      } else {
        Obj("sort" -> Arr(r.sortBy.map(_.toJson)))
      }

    sendRequestWithCustomResponse(
      baseRequest
        .get(uri"${esConfig.uri}/$Search")
        .response(asJson[SearchWithAggregationsResponse])
        .contentType(ApplicationJson)
        .body(
          pointInTimeJson merge
            sortsJson merge
            Obj("query" -> r.query.toJson(fieldPath = None)) merge
            searchAfter.fold(Obj())(sa => Obj("search_after" -> sa)) merge
            config.pageSize.fold(Obj())(ps => Obj("size" -> ps.toJson))
        )
    ).flatMap { response =>
      response.code match {
        case HttpOk =>
          response.body.fold(
            e => ZIO.fail(new ElasticException(s"Exception occurred: ${e.getMessage}")),
            body => {
              body.resultsWithHighlightsAndSort.toList match {
                case Nil => ZIO.succeed((Chunk.empty, None))
                case _ =>
                  body.pitId match {
                    case Some(newPitId) =>
                      body.lastSortField match {
                        case Some(newSearchAfter) =>
                          ZIO.succeed(
                            (
                              itemsFromDocumentsWithHighlights(body.resultsWithHighlightsAndSort),
                              Some((newPitId, Some(newSearchAfter)))
                            )
                          )
                        case None =>
                          ZIO.fail(
                            new ElasticException(
                              s"Unexpected response from Elasticsearch, search_after field is missing."
                            )
                          )
                      }
                    case None =>
                      ZIO.fail(
                        new ElasticException(
                          s"Unexpected response from Elasticsearch, pid.id field is missing."
                        )
                      )
                  }
              }
            }
          )
        case _ =>
          ZIO.fail(handleFailuresFromCustomResponse(response))
      }
    }
  }

  private def executeSearchAndAggregate(r: SearchAndAggregate): Task[SearchAndAggregateResult] =
    sendRequestWithCustomResponse(
      baseRequest
        .post(
          uri"${esConfig.uri}/${r.selectors}/$Search?typed_keys"
            .withParams(getQueryParams(Chunk(("routing", r.routing))))
            .addQuerySegment(QuerySegment.Value("typed_keys"))
        )
        .response(asJson[SearchWithAggregationsResponse])
        .contentType(ApplicationJson)
        .body(r.toJson)
    ).flatMap { response =>
      response.code match {
        case HttpOk =>
          response.body.fold(
            e => ZIO.fail(new ElasticException(s"Exception occurred: ${e.getMessage}")),
            value =>
              ZIO.succeed(
                new SearchAndAggregateResult(
                  itemsFromDocumentsWithHighlights(value.resultsWithHighlightsAndSort),
                  value.aggs.map { case (key, response) =>
                    (key, toResult(response))
                  },
                  value
                )
              )
          )
        case _ =>
          ZIO.fail(handleFailuresFromCustomResponse(response))
      }
    }

  private def executeSearchWithScroll(r: Search, config: StreamConfig): Task[(Chunk[Item], Option[String])] =
    sendRequestWithCustomResponse(
      baseRequest
        .post(
          uri"${esConfig.uri}/${r.selectors}/$Search".withParams(
            getQueryParams(Chunk((Scroll, Some(config.keepAlive)), ("routing", r.routing)))
          )
        )
        .response(asJson[SearchWithAggregationsResponse])
        .contentType(ApplicationJson)
        .body(Obj("query" -> r.query.toJson(fieldPath = None)) merge Obj("sort" -> Arr(r.sortBy.map(_.toJson))))
    ).flatMap { response =>
      response.code match {
        case HttpOk =>
          response.body.fold(
            e => ZIO.fail(new ElasticException(s"Exception occurred: ${e.getMessage}")),
            value =>
              ZIO.succeed(
                (itemsFromDocumentsWithHighlights(value.resultsWithHighlightsAndSort), value.scrollId)
              )
          )
        case _ =>
          ZIO.fail(handleFailuresFromCustomResponse(response))
      }
    }

  private def executeUpdate(r: Update): Task[UpdateOutcome] =
    sendRequest(
      baseRequest
        .post(
          uri"${esConfig.uri}/${r.index}/$Update/${r.id}".withParams(
            getQueryParams(Chunk(("refresh", r.refresh), ("routing", r.routing)))
          )
        )
        .contentType(ApplicationJson)
        .body(r.toJson)
    ).flatMap { response =>
      response.code match {
        case HttpOk      => ZIO.succeed(UpdateOutcome.Updated)
        case HttpCreated => ZIO.succeed(UpdateOutcome.Created)
        case _           => ZIO.fail(handleFailures(response))
      }
    }

  private def executeUpdateByQuery(r: UpdateByQuery): Task[UpdateByQueryResult] =
    sendRequestWithCustomResponse(
      baseRequest
        .post(
          uri"${esConfig.uri}/${r.index}/$UpdateByQuery".withParams(
            getQueryParams(Chunk(("conflicts", r.conflicts), ("refresh", r.refresh), ("routing", r.routing)))
          )
        )
        .response(asJson[UpdateByQueryResponse])
        .contentType(ApplicationJson)
        .body(r.toJson)
    ).flatMap { response =>
      response.code match {
        case HttpOk =>
          response.body.fold(
            e => ZIO.fail(new ElasticException(s"Exception occurred: ${e.getMessage}")),
            value => ZIO.succeed(UpdateByQueryResult.from(response = value))
          )
        case HttpConflict =>
          response.body.fold(
            e => ZIO.fail(new ElasticException(s"Exception occurred: ${e.getMessage}")),
            value => ZIO.fail(VersionConflictException(value.updated + value.deleted, value.versionConflicts))
          )
        case _ =>
          ZIO.fail(handleFailuresFromCustomResponse(response))
      }
    }

  private def getQueryParams(parameters: Chunk[(String, Any)]): ScalaMap[String, String] =
    parameters.collect { case (name, Some(value)) => (name, value.toString) }.toMap

  private def handleFailures(response: Response[Either[String, String]]): ElasticException =
    response.code match {
      case HttpUnauthorized | HttpForbidden =>
        UnauthorizedException
      case _ =>
        new ElasticException(
          s"Unexpected response from Elasticsearch. Response body: ${response.body.fold(body => body, _ => "")}"
        )
    }

  private def handleFailuresFromCustomResponse[A](
    response: Response[Either[ResponseException[String, String], A]]
  ): ElasticException =
    response.code match {
      case HttpUnauthorized | HttpForbidden =>
        UnauthorizedException
      case _ =>
        println(response)
        new ElasticException(
          s"Unexpected response from Elasticsearch. Response body: ${response.body.fold(body => body, _ => "")}"
        )
    }

  private def itemsFromDocumentsWithHighlights(results: Chunk[DocumentWithHighlightsAndSort]): Chunk[Item] =
    results.map(r => Item(raw = r.source, highlight = r.highlight, sort = r.sort))

  private def itemsFromDocumentsWithHighlightsSortAndInnerHits(
    results: Chunk[DocumentWithHighlightsAndSort],
    innerHits: Chunk[Map[String, Chunk[Hit]]]
  ): Chunk[Item] =
    results.zip(innerHits).map { case (r, innerHits) =>
      Item(raw = r.source, highlight = r.highlight, innerHits = innerHits, sort = r.sort)
    }

  private def sendRequest(
    req: RequestT[Identity, Either[String, String], Any]
  ): Task[Response[Either[String, String]]] =
    for {
      _    <- logDebug(s"[es-req]: ${req.show(includeBody = true, includeHeaders = true, sensitiveHeaders = Set.empty)}")
      resp <- req.send(client)
      _    <- logDebug(s"[es-res]: ${resp.show(includeBody = true, includeHeaders = true, sensitiveHeaders = Set.empty)}")
    } yield resp

  private def sendRequestWithCustomResponse[A](
    req: RequestT[Identity, Either[ResponseException[String, String], A], Any]
  ): Task[Response[Either[ResponseException[String, String], A]]] =
    for {
      _    <- logDebug(s"[es-req]: ${req.show(includeBody = true, includeHeaders = true, sensitiveHeaders = Set.empty)}")
      resp <- req.send(client)
      _    <- logDebug(s"[es-res]: ${resp.show(includeBody = true, includeHeaders = true, sensitiveHeaders = Set.empty)}")
    } yield resp
}

private[elasticsearch] object HttpExecutor {

  private final val Bulk          = "_bulk"
  private final val Count         = "_count"
  private final val Create        = "_create"
  private final val DeleteByQuery = "_delete_by_query"
  private final val Doc           = "_doc"
  private final val KeepAlive     = "keep_alive"
  private final val PointInTime   = "_pit"
  private final val Refresh       = "_refresh"
  private final val Scroll        = "scroll"
  private final val ScrollId      = "scroll_id"
  private final val Search        = "_search"
  private final val ShardDoc      = "_shard_doc"
  private final val Update        = "_update"
  private final val UpdateByQuery = "_update_by_query"

  private[elasticsearch] final case class PointInTimeResponse(id: String)
  object PointInTimeResponse {
    implicit val decoder: JsonDecoder[PointInTimeResponse] =
      DeriveJsonDecoder.gen[PointInTimeResponse]
  }

  def apply(esConfig: ElasticConfig, client: SttpBackend[Task, Any]) =
    new HttpExecutor(esConfig, client)
}
