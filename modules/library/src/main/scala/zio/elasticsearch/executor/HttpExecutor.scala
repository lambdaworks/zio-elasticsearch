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

import sttp.client4.httpclient.zio.SttpClient
import sttp.client4.ziojson._
import sttp.client4.{
  ConditionalResponseAs,
  Request,
  Response,
  ResponseException,
  UriContext,
  basicRequest => request,
  fromMetadata
}
import sttp.model.MediaType
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
  MultiGetResponse,
  MultiSearchResponse,
  MultiSearchResponseError,
  SearchWithAggregationsResponse,
  UpdateByQueryResponse
}
import zio.elasticsearch.request.{CreationOutcome, DeletionOutcome, Executable, UpdateOutcome}
import zio.elasticsearch.result._
import zio.json.ast.Json.{Arr, Obj}
import zio.json.ast.{Json, JsonCursor}
import zio.json.{DeriveJsonDecoder, JsonDecoder}
import zio.schema.Schema
import zio.stream.{Stream, ZStream}
import zio.{Chunk, Duration, Ref, Task, UIO, ZIO, durationInt}

import scala.collection.immutable.{Map => ScalaMap}

private[elasticsearch] final class HttpExecutor private (esConfig: ElasticConfig, client: SttpClient) extends Executor {

  import HttpExecutor._

  private val baseRequest = esConfig.credentials match {
    case Some(credentials) => request.auth.basic(credentials.username, credentials.password)
    case _                 => request
  }

  def execute[A](request: ElasticRequest[A, Executable]): Task[A] =
    request match {
      case r: Aggregate                  => executeAggregate(r)
      case r: Bulk                       => executeBulk(r)
      case r: Count                      => executeCount(r)
      case r: Create[Executable]         => executeCreate(r)
      case r: CreateWithId[Executable]   => executeCreateWithId(r)
      case r: CreateIndex                => executeCreateIndex(r)
      case r: CreateOrUpdate[Executable] => executeCreateOrUpdate(r)
      case r: DeleteById[Executable]     => executeDeleteById(r)
      case r: DeleteByQuery              => executeDeleteByQuery(r)
      case r: DeleteIndex                => executeDeleteIndex(r)
      case r: Exists                     => executeExists(r)
      case r: GetById                    => executeGetById(r)
      case r: KNN                        => executeKnn(r)
      case r: MultiGet                   => executeMultiGet(r)
      case r: MultiSearch                => executeMultiSearch(r)
      case r: Refresh                    => executeRefresh(r)
      case r: Search                     => executeSearch(r)
      case r: SearchAndAggregate         => executeSearchAndAggregate(r)
      case r: Update[Executable]         => executeUpdate(r)
      case r: UpdateByQuery              => executeUpdateByQuery(r)
    }

  def stream(r: SearchRequest): Stream[Throwable, Item] =
    stream(r, StreamConfig.Default)

  def stream(request: SearchRequest, config: StreamConfig): Stream[Throwable, Item] =
    request match {
      case r: Search =>
        if (config.searchAfter) streamWithSearchAfter(r, config) else streamWithScroll(r, config)
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
        .body(asJson(r.toJson))
    ).flatMap { response =>
      response.code match {
        case HttpOk =>
          response.body.fold(
            e => ZIO.fail(new ElasticException(s"Exception occurred: ${e.getMessage}")),
            value =>
              ZIO
                .fromEither(value.aggs)
                .mapBoth(
                  error => DecodingException(s"Could not parse aggregations: $error"),
                  aggs => new AggregateResult(aggs.map { case (key, response) => (key, toResult(response)) })
                )
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

  private def executeCount(r: Count): Task[Long] = {
    val req = baseRequest
      .get(uri"${esConfig.uri}/${r.index}/$Count".withParams(getQueryParams(Chunk(("routing", r.routing)))))
      .contentType(ApplicationJson)
      .response(asJson[CountResponse])

    sendRequestWithCustomResponse(r.query.fold(req)(_ => req.body(asJson(r.toJson)))).flatMap { response =>
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

  private def executeCreate(r: Create[Executable]): Task[DocumentId] = {
    val uri = uri"${esConfig.uri}/${r.index}/$Doc"
      .withParams(getQueryParams(Chunk(("refresh", r.refresh), ("routing", r.routing))))

    sendRequestWithCustomResponse[CreateResponse](
      baseRequest
        .post(uri)
        .contentType(ApplicationJson)
        .body(r.document.raw)
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

  private def executeCreateWithId(r: CreateWithId[Executable]): Task[CreationOutcome] = {
    val uri = uri"${esConfig.uri}/${r.index}/$Create/${r.id}"
      .withParams(getQueryParams(Chunk(("refresh", r.refresh), ("routing", r.routing))))

    sendRequest(
      baseRequest
        .post(uri)
        .contentType(ApplicationJson)
        .body(r.document.raw)
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

  private def executeCreateOrUpdate(r: CreateOrUpdate[Executable]): Task[Unit] = {
    val uri = uri"${esConfig.uri}/${r.index}/$Doc/${r.id}"
      .withParams(getQueryParams(Chunk(("refresh", r.refresh), ("routing", r.routing))))

    sendRequest(baseRequest.put(uri).contentType(ApplicationJson).body(r.document.raw)).flatMap { response =>
      response.code match {
        case HttpOk | HttpCreated => ZIO.unit
        case _                    => ZIO.fail(handleFailures(response))
      }
    }
  }

  private def executeClearScroll(scrollId: String): UIO[Unit] =
    executeReleaseSearchContext(
      baseRequest
        .delete(uri"${esConfig.uri}/$Search/$Scroll")
        .contentType(ApplicationJson)
        .body(asJson(Obj(ScrollId -> scrollId.toJson))),
      failureMessage = "Could not clear the scroll context."
    )

  private def executeClosePointInTime(pitId: String): UIO[Unit] =
    executeReleaseSearchContext(
      baseRequest
        .delete(uri"${esConfig.uri}/$PointInTime")
        .contentType(ApplicationJson)
        .body(asJson(Obj("id" -> pitId.toJson))),
      failureMessage = "Could not close the point in time."
    )

  private def executeOpenPointInTime(r: Search, config: StreamConfig): Task[String] =
    sendRequestWithCustomResponse(
      baseRequest
        .post(
          uri"${esConfig.uri}/${r.selectors}/$PointInTime".withParams(
            getQueryParams(Chunk((KeepAlive, Some(config.keepAlive)), ("routing", r.routing)))
          )
        )
        .response(asJson[PointInTimeResponse])
        .contentType(ApplicationJson)
    ).flatMap { response =>
      response.code match {
        case HttpOk =>
          response.body.fold(
            e => ZIO.fail(new ElasticException(s"Exception occurred: ${e.getMessage}")),
            pit => ZIO.succeed(pit.id)
          )
        case _ =>
          ZIO.fail(handleFailuresFromCustomResponse(response))
      }
    }

  private def executeDeleteById(r: DeleteById[Executable]): Task[DeletionOutcome] = {
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
        .body(asJson(r.toJson))
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
        .body(asJson(Obj(ScrollId -> scrollId.toJson)))
    ).flatMap { response =>
      response.code match {
        case HttpOk =>
          response.body.fold(
            e => ZIO.fail(new ElasticException(s"Exception occurred: ${e.getMessage}")),
            value => ZIO.succeed((itemsFromDocumentsWithHighlights(value.resultsWithHighlightsAndSort), value.scrollId))
          )
        case _ =>
          ZIO.fail(handleFailuresFromCustomResponse(response))
      }
    }

  private def executeKnn(r: KNN): Task[KNNSearchResult] = {
    val uri = uri"${esConfig.uri}/${r.selectors}/$Search".withParams(
      getQueryParams(Chunk(("routing", r.routing)))
    )

    sendRequestWithCustomResponse[SearchWithAggregationsResponse](
      baseRequest
        .post(uri)
        .response(asJson[SearchWithAggregationsResponse])
        .contentType(ApplicationJson)
        .body(asJson(r.toJson))
    ).flatMap { response =>
      response.code match {
        case HttpOk =>
          response.body.fold(
            e => ZIO.fail(new ElasticException(s"Exception occurred: ${e.getMessage}")),
            value =>
              ZIO.succeed(new KNNSearchResult(itemsFromDocumentsWithHighlights(value.resultsWithHighlightsAndSort)))
          )
        case _ =>
          ZIO.fail(handleFailuresFromCustomResponse(response))
      }
    }
  }

  private def executeMultiGet(r: MultiGet): Task[MultiGetResult] = {
    val uri = uri"${esConfig.uri}/${r.index}/$Mget".withParams(
      getQueryParams(
        Chunk(
          ("_source_excludes", Some(r.excluded.mkString(",")).filter(_.nonEmpty)),
          ("_source_includes", Some(r.included.mkString(",")).filter(_.nonEmpty)),
          ("realtime", r.realtime),
          ("refresh", r.refresh),
          ("routing", r.routing)
        )
      )
    )

    sendRequestWithCustomResponse(
      baseRequest
        .post(uri)
        .response(asJson[MultiGetResponse])
        .contentType(ApplicationJson)
        .body(asJson(r.toJson))
    ).flatMap { response =>
      response.code match {
        case HttpOk =>
          response.body.fold(
            e => ZIO.fail(new ElasticException(s"Exception occurred: ${e.getMessage}")),
            value =>
              ZIO.succeed(new MultiGetResult(value.docs.map { doc =>
                val id = DocumentId(doc.id)
                (doc.error, doc.found) match {
                  case (Some(error), _) => MultiGetItem.Failed(index = doc.index, id = id, error = error)
                  case (_, Some(true))  =>
                    MultiGetItem.Found(index = doc.index, id = id, item = doc.source.map(source => Item(raw = source)))
                  case _ => MultiGetItem.NotFound(index = doc.index, id = id)
                }
              }))
          )
        case _ =>
          ZIO.fail(handleFailuresFromCustomResponse(response))
      }
    }
  }

  private def executeMultiSearch(r: MultiSearch): Task[MultiSearchResult] =
    sendRequestWithCustomResponse(
      baseRequest
        .post(
          uri"${esConfig.uri}/$Msearch"
            .withParams(getQueryParams(Chunk(("max_concurrent_searches", r.maxConcurrentSearches))))
            .addQuerySegment(QuerySegment.Value("typed_keys"))
        )
        .response(asJson[MultiSearchResponse])
        .contentType(ApplicationNdJson)
        .body(r.body)
    ).flatMap { response =>
      response.code match {
        case HttpOk =>
          response.body.fold(
            e => ZIO.fail(new ElasticException(s"Exception occurred: ${e.getMessage}")),
            value => ZIO.succeed(new MultiSearchResult(value.responses.map(multiSearchItem)))
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

  /**
   * Runs in the finalizer of a stream, which is uninterruptible, so the request is made interruptible and bounded by
   * `ReleaseTimeout` to keep an unresponsive cluster from blocking the interruption of the stream.
   */
  private def executeReleaseSearchContext(req: Request[Either[String, String]], failureMessage: String): UIO[Unit] =
    sendRequest(req).flatMap { response =>
      response.code match {
        case HttpOk | HttpNotFound => ZIO.unit
        case _                     => ZIO.fail(handleFailures(response))
      }
    }.interruptible
      .timeoutFail(new ElasticException(s"Request timed out after $ReleaseTimeout."))(ReleaseTimeout)
      .catchAllCause(cause => ZIO.logWarningCause(failureMessage, cause))

  private def executeSearch(r: Search): Task[SearchResult] =
    sendRequestWithCustomResponse(
      baseRequest
        .post(uri"${esConfig.uri}/${r.selectors}/$Search".withParams(getQueryParams(Chunk(("routing", r.routing)))))
        .response(asJson[SearchWithAggregationsResponse])
        .contentType(ApplicationJson)
        .body(asJson(r.toJson))
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
          ZIO.fail(handleFailuresFromCustomResponse(response))
      }
    }

  private def executeSearchAfterRequest(
    r: Search,
    pitId: String,
    config: StreamConfig,
    searchAfter: Option[Json]
  ): Task[SearchAfterPage] = {
    val pointInTimeJson =
      Obj(
        "pit" -> Obj(
          "id"      -> pitId.toJson,
          KeepAlive -> config.keepAlive.toJson
        )
      )

    val defaultSortJson =
      if (r.sortBy.isEmpty) Obj("sort" -> Arr(ShardDoc.toJson), "track_total_hits" -> false.toJson) else Obj()

    sendRequestWithCustomResponse(
      baseRequest
        .post(uri"${esConfig.uri}/$Search")
        .response(asJson[SearchWithAggregationsResponse])
        .contentType(ApplicationJson)
        .body(
          asJson(
            streamRequestBody(r, config) merge
              pointInTimeJson merge
              defaultSortJson merge
              searchAfter.fold(Obj())(sa => Obj("search_after" -> sa))
          )
        )
    ).flatMap { response =>
      response.code match {
        case HttpOk =>
          response.body.fold(
            e => ZIO.fail(new ElasticException(s"Exception occurred: ${e.getMessage}")),
            body => {
              val items = itemsFromDocumentsWithHighlights(body.resultsWithHighlightsAndSort)
              val page  = SearchAfterPage(items = items, pitId = body.pitId.getOrElse(pitId), searchAfter = None)

              if (items.isEmpty) {
                ZIO.succeed(page)
              } else {
                body.lastSortField match {
                  case Some(newSearchAfter) =>
                    ZIO.succeed(page.copy(searchAfter = Some(newSearchAfter)))
                  case None =>
                    ZIO.fail(
                      new ElasticException("Unexpected response from Elasticsearch, search_after field is missing.")
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
        .body(asJson(r.toJson))
    ).flatMap { response =>
      response.code match {
        case HttpOk =>
          response.body.fold(
            e => ZIO.fail(new ElasticException(s"Exception occurred: ${e.getMessage}")),
            value =>
              ZIO
                .fromEither(value.aggs)
                .mapBoth(
                  error => DecodingException(s"Could not parse aggregations: $error"),
                  aggs =>
                    new SearchAndAggregateResult(
                      itemsFromDocumentsWithHighlights(value.resultsWithHighlightsAndSort),
                      aggs.map { case (key, response) => (key, toResult(response)) },
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
        .body(
          asJson(
            streamRequestBody(r, config) merge
              (if (r.sortBy.isEmpty) Obj("sort" -> Arr(Doc.toJson)) else Obj())
          )
        )
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

  private def executeUpdate(r: Update[Executable]): Task[UpdateOutcome] =
    sendRequest(
      baseRequest
        .post(
          uri"${esConfig.uri}/${r.index}/$Update/${r.id}".withParams(
            getQueryParams(Chunk(("refresh", r.refresh), ("routing", r.routing)))
          )
        )
        .contentType(ApplicationJson)
        .body(r.body)
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
        .response(
          fromMetadata(
            asJson[UpdateByQueryResponse],
            ConditionalResponseAs(_.code == HttpConflict, asJsonAlways[UpdateByQueryResponse])
          )
        )
        .contentType(ApplicationJson)
        .body(asJson(r.toJson))
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
    response: Response[Either[ResponseException[String], A]]
  ): ElasticException =
    response.code match {
      case HttpUnauthorized | HttpForbidden =>
        UnauthorizedException
      case _ =>
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

  private def multiSearchItem(json: Json): MultiSearchItem = {
    val status = json.get(MultiSearchStatus).flatMap(_.as[Int]).getOrElse(HttpOk.code)

    val item: Either[String, MultiSearchItem] = json.get(MultiSearchError) match {
      case Right(_) =>
        json.as[MultiSearchResponseError].map(r => MultiSearchItem.Failed(status = r.status, error = r.error.toError))
      case Left(_) =>
        for {
          response  <- json.as[SearchWithAggregationsResponse]
          innerHits <- response.innerHitsResults
          aggs      <- response.aggs
        } yield MultiSearchItem.Succeeded(
          status = status,
          result = new SearchAndAggregateResult(
            itemsFromDocumentsWithHighlightsSortAndInnerHits(response.resultsWithHighlightsAndSort, innerHits),
            aggs.map { case (key, response) => (key, toResult(response)) },
            response
          )
        )
    }

    item.fold(error => MultiSearchItem.Undecodable(status = status, response = json, message = error), identity)
  }

  private def sendRequest(req: Request[Either[String, String]]): Task[Response[Either[String, String]]] =
    for {
      _    <- logDebug(s"[es-req]: ${req.show(includeBody = true, includeHeaders = true, sensitiveHeaders = Set.empty)}")
      resp <- req.send(client)
      _    <- logDebug(s"[es-res]: ${resp.show(includeBody = true, includeHeaders = true, sensitiveHeaders = Set.empty)}")
    } yield resp

  private def sendRequestWithCustomResponse[A](
    req: Request[Either[ResponseException[String], A]]
  ): Task[Response[Either[ResponseException[String], A]]] =
    for {
      _    <- logDebug(s"[es-req]: ${req.show(includeBody = true, includeHeaders = true, sensitiveHeaders = Set.empty)}")
      resp <- req.send(client)
      _    <- logDebug(s"[es-res]: ${resp.show(includeBody = true, includeHeaders = true, sensitiveHeaders = Set.empty)}")
    } yield resp

  private def streamRequestBody(r: Search, config: StreamConfig): Json =
    r.copy(from = None, searchAfter = None, size = config.pageSize).toJson

  private def streamWithScroll(r: Search, config: StreamConfig): Stream[Throwable, Item] =
    ZStream
      .acquireReleaseWith(
        executeSearchWithScroll(r, config).flatMap { case (items, scrollId) =>
          Ref.make(scrollId).map(scrollIdRef => (items, scrollId, scrollIdRef))
        }
      ) { case (_, _, scrollIdRef) => scrollIdRef.get.flatMap(ZIO.foreachDiscard(_)(executeClearScroll)) }
      .flatMap { case (firstItems, firstScrollId, scrollIdRef) =>
        val nextPages = firstScrollId.filter(_ => firstItems.nonEmpty) match {
          case Some(scrollId) =>
            ZStream.paginateChunkZIO(scrollId) { currentScrollId =>
              executeGetByScroll(currentScrollId, config).flatMap { case (items, newScrollId) =>
                val nextScrollId = newScrollId.getOrElse(currentScrollId)

                scrollIdRef.set(Some(nextScrollId)).as((items, if (items.isEmpty) None else Some(nextScrollId)))
              }
            }
          case None =>
            ZStream.empty
        }

        ZStream.fromChunk(firstItems) ++ nextPages
      }

  private def streamWithSearchAfter(r: Search, config: StreamConfig): Stream[Throwable, Item] =
    if (r.searchAfter.isDefined && r.sortBy.isEmpty) {
      ZStream.fail(
        new ElasticException(
          "Streaming with search_after requires the request to be sorted when its searchAfter value is set."
        )
      )
    } else {
      ZStream
        .acquireReleaseWith(executeOpenPointInTime(r, config).flatMap(Ref.make(_)))(
          _.get.flatMap(executeClosePointInTime)
        )
        .flatMap { pitIdRef =>
          ZStream.paginateChunkZIO(r.searchAfter) { searchAfter =>
            pitIdRef.get
              .flatMap(pitId =>
                executeSearchAfterRequest(r = r, pitId = pitId, config = config, searchAfter = searchAfter)
              )
              .flatMap { page =>
                pitIdRef
                  .set(page.pitId)
                  .as((page.items, if (page.items.isEmpty) None else page.searchAfter.map(Some(_))))
              }
          }
        }
    }
}

private[elasticsearch] object HttpExecutor {

  private final val Bulk          = "_bulk"
  private final val Count         = "_count"
  private final val Create        = "_create"
  private final val DeleteByQuery = "_delete_by_query"
  private final val Doc           = "_doc"
  private final val KeepAlive     = "keep_alive"
  private final val Mget          = "_mget"
  private final val Msearch       = "_msearch"
  private final val PointInTime   = "_pit"
  private final val Refresh       = "_refresh"
  private final val Scroll        = "scroll"
  private final val ScrollId      = "scroll_id"
  private final val Search        = "_search"
  private final val ShardDoc      = "_shard_doc"
  private final val Update        = "_update"
  private final val UpdateByQuery = "_update_by_query"

  private final val ApplicationNdJson = MediaType("application", "x-ndjson")
  private final val MultiSearchError  = JsonCursor.field("error")
  private final val MultiSearchStatus = JsonCursor.field("status")

  private[elasticsearch] final val ReleaseTimeout: Duration = 10.seconds

  private final case class SearchAfterPage(items: Chunk[Item], pitId: String, searchAfter: Option[Json])

  private[elasticsearch] final case class PointInTimeResponse(id: String)
  object PointInTimeResponse {
    implicit val decoder: JsonDecoder[PointInTimeResponse] =
      DeriveJsonDecoder.gen[PointInTimeResponse]
  }

  def apply(esConfig: ElasticConfig, client: SttpClient) =
    new HttpExecutor(esConfig, client)
}
