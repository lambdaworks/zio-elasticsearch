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
  NotFound => HttpNotFound,
  Ok => HttpOk,
  Unauthorized => HttpUnauthorized
}
import sttp.model.Uri.QuerySegment
import zio.ZIO.logDebug
import zio.elasticsearch.ElasticRequest._
import zio.elasticsearch._
import zio.elasticsearch.executor.response.{CountResponse, CreateResponse, GetResponse, SearchWithAggregationsResponse}
import zio.elasticsearch.result._
import zio.json.ast.Json
import zio.json.ast.Json.{Arr, Obj, Str}
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
      case r: Search             => executeSearch(r)
      case r: SearchAndAggregate => executeSearchAndAggregate(r)
    }

  def stream(r: SearchRequest): Stream[Throwable, Item] =
    stream(r, StreamConfig.Default)

  def stream(request: SearchRequest, config: StreamConfig): Stream[Throwable, Item] =
    request match {
      case r: Search =>
        if (config.searchAfter) {
          ZStream.paginateChunkZIO[Any, Throwable, Item, (String, Option[Json])](("", None)) {
            case ("", _) =>
              executeCreatePointInTime(r.index, config)
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

  private def executeAggregate(r: Aggregate): Task[AggregationResult] =
    sendRequestWithCustomResponse(
      baseRequest
        .post(uri"${esConfig.uri}/${r.index}/$Search?typed_keys")
        .response(asJson[SearchWithAggregationsResponse])
        .contentType(ApplicationJson)
        .body(r.aggregation.toJson)
    ).flatMap { response =>
      response.code match {
        case HttpOk =>
          response.body.fold(
            e => ZIO.fail(new ElasticException(s"Exception occurred: ${e.getMessage}")),
            value => ZIO.succeed(new AggregationResult(value.aggs))
          )
        case _ =>
          ZIO.fail(createElasticExceptionFromCustomResponse(response))
      }
    }

  private def executeBulk(r: Bulk): Task[Unit] = {
    val uri = (r.index match {
      case Some(index) => uri"${esConfig.uri}/$index/$Bulk"
      case None        => uri"${esConfig.uri}/$Bulk"
    }).withParams(getQueryParams(List(("refresh", r.refresh), ("routing", r.routing))))

    sendRequest(
      baseRequest.post(uri).contentType(ApplicationJson).body(r.body)
    ).flatMap { response =>
      response.code match {
        case HttpOk => ZIO.unit
        case _      => ZIO.fail(createElasticException(response))
      }
    }
  }

  private def executeCount(r: Count): Task[Int] = {
    val req = baseRequest
      .get(uri"${esConfig.uri}/${r.index}/$Count".withParams(getQueryParams(List(("routing", r.routing)))))
      .contentType(ApplicationJson)
      .response(asJson[CountResponse])

    sendRequestWithCustomResponse(r.query.fold(req)(query => req.body(query.toJson))).flatMap { response =>
      response.code match {
        case HttpOk =>
          response.body
            .map(_.count)
            .fold(
              e => ZIO.fail(new ElasticException(s"Exception occurred: ${e.getMessage}")),
              value => ZIO.succeed(value)
            )
        case _ =>
          ZIO.fail(createElasticExceptionFromCustomResponse(response))
      }
    }
  }

  private def executeCreate(r: Create): Task[DocumentId] = {
    val uri = uri"${esConfig.uri}/${r.index}/$Doc"
      .withParams(getQueryParams(List(("refresh", r.refresh), ("routing", r.routing))))

    sendRequestWithCustomResponse[CreateResponse](
      baseRequest
        .post(uri)
        .contentType(ApplicationJson)
        .body(r.document.json)
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
          ZIO.fail(createElasticExceptionFromCustomResponse(response))
      }
    }

  }

  private def executeCreateWithId(r: CreateWithId): Task[CreationOutcome] = {
    val uri = uri"${esConfig.uri}/${r.index}/$Create/${r.id}"
      .withParams(getQueryParams(List(("refresh", r.refresh), ("routing", r.routing))))

    sendRequest(
      baseRequest
        .post(uri)
        .contentType(ApplicationJson)
        .body(r.document.json)
    ).flatMap { response =>
      response.code match {
        case HttpCreated  => ZIO.succeed(Created)
        case HttpConflict => ZIO.succeed(AlreadyExists)
        case _            => ZIO.fail(createElasticException(response))
      }
    }
  }

  private def executeCreateIndex(createIndex: CreateIndex): Task[CreationOutcome] =
    sendRequest(
      baseRequest
        .put(uri"${esConfig.uri}/${createIndex.name}")
        .contentType(ApplicationJson)
        .body(createIndex.definition.getOrElse(""))
    ).flatMap { response =>
      response.code match {
        case HttpOk         => ZIO.succeed(Created)
        case HttpBadRequest => ZIO.succeed(AlreadyExists)
        case _              => ZIO.fail(createElasticException(response))
      }
    }

  private def executeCreateOrUpdate(r: CreateOrUpdate): Task[Unit] = {
    val uri = uri"${esConfig.uri}/${r.index}/$Doc/${r.id}"
      .withParams(getQueryParams(List(("refresh", r.refresh), ("routing", r.routing))))

    sendRequest(baseRequest.put(uri).contentType(ApplicationJson).body(r.document.json)).flatMap { response =>
      response.code match {
        case HttpOk | HttpCreated => ZIO.unit
        case _                    => ZIO.fail(createElasticException(response))
      }
    }
  }

  private def executeCreatePointInTime(
    index: IndexName,
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
          ZIO.fail(createElasticExceptionFromCustomResponse(response))
      }
    }

  private def executeDeleteById(r: DeleteById): Task[DeletionOutcome] = {
    val uri = uri"${esConfig.uri}/${r.index}/$Doc/${r.id}"
      .withParams(getQueryParams(List(("refresh", r.refresh), ("routing", r.routing))))

    sendRequest(baseRequest.delete(uri)).flatMap { response =>
      response.code match {
        case HttpOk       => ZIO.succeed(Deleted)
        case HttpNotFound => ZIO.succeed(NotFound)
        case _            => ZIO.fail(createElasticException(response))
      }
    }
  }

  private def executeDeleteByQuery(r: DeleteByQuery): Task[DeletionOutcome] = {
    val uri =
      uri"${esConfig.uri}/${r.index}/$DeleteByQuery".withParams(
        getQueryParams(List(("refresh", r.refresh), ("routing", r.routing)))
      )

    sendRequest(
      baseRequest
        .post(uri)
        .contentType(ApplicationJson)
        .body(r.query.toJson)
    ).flatMap { response =>
      response.code match {
        case HttpOk       => ZIO.succeed(Deleted)
        case HttpNotFound => ZIO.succeed(NotFound)
        case _            => ZIO.fail(createElasticException(response))
      }
    }
  }

  private def executeDeleteIndex(r: DeleteIndex): Task[DeletionOutcome] =
    sendRequest(baseRequest.delete(uri"${esConfig.uri}/${r.name}")).flatMap { response =>
      response.code match {
        case HttpOk       => ZIO.succeed(Deleted)
        case HttpNotFound => ZIO.succeed(NotFound)
        case _            => ZIO.fail(createElasticException(response))
      }
    }

  private def executeExists(r: Exists): Task[Boolean] = {
    val uri = uri"${esConfig.uri}/${r.index}/$Doc/${r.id}".withParams(getQueryParams(List(("routing", r.routing))))

    sendRequest(baseRequest.head(uri)).flatMap { response =>
      response.code match {
        case HttpOk       => ZIO.succeed(true)
        case HttpNotFound => ZIO.succeed(false)
        case _            => ZIO.fail(createElasticException(response))
      }
    }
  }

  private def executeGetById(r: GetById): Task[GetResult] = {
    val uri = uri"${esConfig.uri}/${r.index}/$Doc/${r.id}".withParams(
      getQueryParams(List(("refresh", r.refresh), ("routing", r.routing)))
    )

    sendRequestWithCustomResponse[GetResponse](
      baseRequest
        .get(uri)
        .response(asJson[GetResponse])
    ).flatMap { response =>
      response.code match {
        case HttpOk       => ZIO.attempt(new GetResult(doc = response.body.toOption.map(r => result.Item(r.source))))
        case HttpNotFound => ZIO.succeed(new GetResult(doc = None))
        case _            => ZIO.fail(createElasticExceptionFromCustomResponse(response))
      }
    }
  }

  private def executeGetByScroll(scrollId: String, config: StreamConfig): Task[(Chunk[Item], Option[String])] =
    sendRequestWithCustomResponse(
      baseRequest
        .post(uri"${esConfig.uri}/$Search/$Scroll".withParams((Scroll, config.keepAlive)))
        .response(asJson[SearchWithAggregationsResponse])
        .contentType(ApplicationJson)
        .body(Obj(ScrollId -> Str(scrollId)))
    ).flatMap { response =>
      response.code match {
        case HttpOk =>
          response.body.fold(
            e => ZIO.fail(new ElasticException(s"Exception occurred: ${e.getMessage}")),
            value =>
              value.results match {
                case Nil =>
                  ZIO.succeed((Chunk.empty, None))
                case _ =>
                  ZIO.succeed(
                    (Chunk.fromIterable(value.results).map(Item.apply), value.scrollId.orElse(Some(scrollId)))
                  )
              }
          )
        case _ =>
          ZIO.fail(createElasticExceptionFromCustomResponse(response))
      }
    }

  private def executeSearch(r: Search): Task[SearchResult] = {
    val body = r.sortBy match {
      case sorts if sorts.nonEmpty =>
        Obj(
          List(
            "query" -> r.query.paramsToJson(fieldPath = None),
            "sort"  -> Arr(sorts.toList.map(_.paramsToJson): _*)
          ): _*
        )
      case _ =>
        r.query.toJson
    }

    sendRequestWithCustomResponse(
      baseRequest
        .post(uri"${esConfig.uri}/${r.index}/$Search".withParams(getQueryParams(List(("routing", r.routing)))))
        .response(asJson[SearchWithAggregationsResponse])
        .contentType(ApplicationJson)
        .body(body)
    ).flatMap { response =>
      response.code match {
        case HttpOk =>
          response.body.fold(
            e => ZIO.fail(new ElasticException(s"Exception occurred: ${e.getMessage}")),
            value => ZIO.succeed(new SearchResult(value.results.map(Item.apply)))
          )
        case HttpUnauthorized =>
          ZIO.fail(UnauthorizedException("Unauthorized action!"))
        case _ =>
          ZIO.fail(createElasticExceptionFromCustomResponse(response))
      }
    }
  }

  private def executeSearchAfterRequest(
    r: Search,
    pitId: String,
    config: StreamConfig,
    searchAfter: Option[Json]
  ): Task[(Chunk[Item], Option[(String, Option[Json])])] = {
    val pointInTimeJson =
      Json.Obj(
        "pit" -> Json.Obj(
          "id"      -> Json.Str(pitId),
          KeepAlive -> Json.Str(config.keepAlive)
        )
      )
    val defaultSortField = Json.Obj("sort" -> Json.Arr(Json.Str(ShardDoc)))
    val searchAfterJson  = searchAfter.map(sa => Json.Obj("search_after" -> sa))
    val requestBody      = r.query.toJson merge pointInTimeJson merge defaultSortField
    sendRequestWithCustomResponse(
      baseRequest
        .get(uri"${esConfig.uri}/$Search")
        .response(asJson[SearchWithAggregationsResponse])
        .contentType(ApplicationJson)
        .body(searchAfterJson.map(_ merge requestBody).getOrElse(requestBody))
    ).flatMap { response =>
      response.code match {
        case HttpOk =>
          response.body.fold(
            e => ZIO.fail(new ElasticException(s"Exception occurred: ${e.getMessage}")),
            body => {
              body.results match {
                case Nil => ZIO.succeed((Chunk.empty, None))
                case _ =>
                  body.pitId match {
                    case Some(newPitId) =>
                      body.lastSortField match {
                        case Some(newSearchAfter) =>
                          ZIO.succeed(
                            (Chunk.fromIterable(body.results.map(Item.apply)), Some((newPitId, Some(newSearchAfter))))
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
          ZIO.fail(createElasticExceptionFromCustomResponse(response))
      }
    }
  }

  private def executeSearchAndAggregate(r: SearchAndAggregate): Task[SearchAndAggregateResult] = {
    val body = r.sortBy match {
      case sorts if sorts.nonEmpty =>
        Obj(
          List(
            "query" -> r.query.paramsToJson(fieldPath = None),
            "sort"  -> Arr(sorts.toList.map(_.paramsToJson): _*),
            "aggs"  -> r.aggregation.paramsToJson
          ): _*
        )
      case _ =>
        Obj(
          List(
            "query" -> r.query.paramsToJson(fieldPath = None),
            "aggs"  -> r.aggregation.paramsToJson
          ): _*
        )
    }

    sendRequestWithCustomResponse(
      baseRequest
        .post(
          uri"${esConfig.uri}/${r.index}/$Search?typed_keys"
            .withParams(getQueryParams(List(("routing", r.routing))))
            .addQuerySegment(QuerySegment.Value("typed_keys"))
        )
        .response(asJson[SearchWithAggregationsResponse])
        .contentType(ApplicationJson)
        .body(body)
    ).flatMap { response =>
      response.code match {
        case HttpOk =>
          response.body.fold(
            e => ZIO.fail(new ElasticException(s"Exception occurred: ${e.getMessage}")),
            value => ZIO.succeed(new SearchAndAggregateResult(value.results.map(Item.apply), value.aggs))
          )
        case _ =>
          ZIO.fail(createElasticExceptionFromCustomResponse(response))
      }
    }
  }

  private def executeSearchWithScroll(r: Search, config: StreamConfig): Task[(Chunk[Item], Option[String])] =
    sendRequestWithCustomResponse(
      baseRequest
        .post(
          uri"${esConfig.uri}/${r.index}/$Search".withParams(
            getQueryParams(List((Scroll, Some(config.keepAlive)), ("routing", r.routing)))
          )
        )
        .response(asJson[SearchWithAggregationsResponse])
        .contentType(ApplicationJson)
        .body(r.query.toJson)
    ).flatMap { response =>
      response.code match {
        case HttpOk =>
          response.body.fold(
            e => ZIO.fail(new ElasticException(s"Exception occurred: ${e.getMessage}")),
            value => ZIO.succeed((Chunk.fromIterable(value.results).map(Item.apply), value.scrollId))
          )
        case _ =>
          ZIO.fail(createElasticExceptionFromCustomResponse(response))
      }
    }

  private def createElasticException(response: Response[Either[String, String]]): ElasticException =
    new ElasticException(
      s"Unexpected response from Elasticsearch. Response body: ${response.body.fold(body => body, _ => "")}"
    )

  private def createElasticExceptionFromCustomResponse[A](
    response: Response[Either[ResponseException[String, String], A]]
  ): ElasticException =
    new ElasticException(
      s"Unexpected response from Elasticsearch. Response body: ${response.body.fold(body => body, _ => "")}"
    )

  private def getQueryParams(parameters: List[(String, Any)]): ScalaMap[String, String] =
    parameters.collect { case (name, Some(value)) => (name, value.toString) }.toMap

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
  private final val Scroll        = "scroll"
  private final val ScrollId      = "scroll_id"
  private final val Search        = "_search"
  private final val ShardDoc      = "_shard_doc"

  private[elasticsearch] final case class PointInTimeResponse(id: String)
  object PointInTimeResponse {
    implicit val decoder: JsonDecoder[PointInTimeResponse] =
      DeriveJsonDecoder.gen[PointInTimeResponse]
  }

  def apply(esConfig: ElasticConfig, client: SttpBackend[Task, Any]) =
    new HttpExecutor(esConfig, client)
}
