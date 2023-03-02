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

import sttp.client3.ziojson._
import sttp.client3.{Identity, RequestT, Response, ResponseException, SttpBackend, UriContext, basicRequest => request}
import sttp.model.MediaType.ApplicationJson
import sttp.model.StatusCode.{
  BadRequest => HttpBadRequest,
  Conflict => HttpConflict,
  Created => HttpCreated,
  NotFound => HttpNotFound,
  Ok => HttpOk
}
import zio.ZIO.logDebug
import zio.elasticsearch.ElasticRequest._
import zio.json.ast.Json.{Obj, Str}
import zio.stream.ZStream
import zio.{Chunk, Task, ZIO}

import scala.collection.immutable.{Map => ScalaMap}

private[elasticsearch] final class HttpElasticExecutor private (config: ElasticConfig, client: SttpBackend[Task, Any])
    extends ElasticExecutor {

  import HttpElasticExecutor._

  def execute[A](request: ElasticRequest[A]): Task[A] =
    request match {
      case r: Bulk           => executeBulk(r)
      case r: Create         => executeCreate(r)
      case r: CreateWithId   => executeCreateWithId(r)
      case r: CreateIndex    => executeCreateIndex(r)
      case r: CreateOrUpdate => executeCreateOrUpdate(r)
      case r: DeleteById     => executeDeleteById(r)
      case r: DeleteByQuery  => executeDeleteByQuery(r)
      case r: DeleteIndex    => executeDeleteIndex(r)
      case r: Exists         => executeExists(r)
      case r: GetById        => executeGetById(r)
      case r: GetByQuery     => executeGetByQuery(r)
    }

//  def stream[A: Schema](r: GetByQuery): ZStream[Any, Throwable, A] = {
//    val pipeline: ZPipeline[Any, Nothing, Json, Document]                    = ZPipeline.map(Document.from)
//    val pipeline2: ZPipeline[Any, Nothing, Document, Either[DecodeError, A]] = ZPipeline.map(_.decode)
//    val pipeline3: ZPipeline[Any, Nothing, Either[DecodeError, A], A]        = ZPipeline.collectWhileRight
//
//    ZStream.paginateChunkZIO("") { s =>
//      if (s.isEmpty) executeGetByQueryWithScroll(r) else executeGetByScroll(s)
//    } >>> pipeline >>> pipeline2 >>> pipeline3
//  }

  def stream(r: GetByQuery): ZStream[Any, Throwable, RawItem] =
    ZStream.paginateChunkZIO("") { s =>
      if (s.isEmpty) executeGetByQueryWithScroll(r) else executeGetByScroll(s)
    }

  private def executeBulk(r: Bulk): Task[Unit] = {
    val uri = (r.index match {
      case Some(index) => uri"${config.uri}/$index/$Bulk"
      case None        => uri"${config.uri}/$Bulk"
    }).withParams(getQueryParams(List(("refresh", Some(r.refresh)), ("routing", r.routing))))

    sendRequest(
      request.post(uri).contentType(ApplicationJson).body(r.body)
    ).flatMap { response =>
      response.code match {
        case HttpOk => ZIO.unit
        case _      => ZIO.fail(createElasticException(response))
      }
    }
  }

  private def executeCreate(r: Create): Task[DocumentId] = {
    val uri = uri"${config.uri}/${r.index}/$Doc"
      .withParams(getQueryParams(List(("refresh", Some(r.refresh)), ("routing", r.routing))))

    sendRequestWithCustomResponse[ElasticCreateResponse](
      request
        .post(uri)
        .contentType(ApplicationJson)
        .body(r.document.json)
        .response(asJson[ElasticCreateResponse])
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
    val uri = uri"${config.uri}/${r.index}/$Create/${r.id}"
      .withParams(getQueryParams(List(("refresh", Some(r.refresh)), ("routing", r.routing))))

    sendRequest(
      request
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
      request
        .put(uri"${config.uri}/${createIndex.name}")
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
    val uri = uri"${config.uri}/${r.index}/$Doc/${r.id}"
      .withParams(getQueryParams(List(("refresh", Some(r.refresh)), ("routing", r.routing))))

    sendRequest(request.put(uri).contentType(ApplicationJson).body(r.document.json)).flatMap { response =>
      response.code match {
        case HttpOk | HttpCreated => ZIO.unit
        case _                    => ZIO.fail(createElasticException(response))
      }
    }
  }

  private def executeDeleteById(r: DeleteById): Task[DeletionOutcome] = {
    val uri = uri"${config.uri}/${r.index}/$Doc/${r.id}"
      .withParams(getQueryParams(List(("refresh", Some(r.refresh)), ("routing", r.routing))))

    sendRequest(request.delete(uri)).flatMap { response =>
      response.code match {
        case HttpOk       => ZIO.succeed(Deleted)
        case HttpNotFound => ZIO.succeed(NotFound)
        case _            => ZIO.fail(createElasticException(response))
      }
    }
  }

  private def executeDeleteByQuery(r: DeleteByQuery): Task[DeletionOutcome] = {
    val uri =
      uri"${config.uri}/${r.index}/$DeleteByQuery".withParams(
        getQueryParams(List(("refresh", Some(r.refresh)), ("routing", r.routing)))
      )

    sendRequest(
      request
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
    sendRequest(request.delete(uri"${config.uri}/${r.name}")).flatMap { response =>
      response.code match {
        case HttpOk       => ZIO.succeed(Deleted)
        case HttpNotFound => ZIO.succeed(NotFound)
        case _            => ZIO.fail(createElasticException(response))
      }
    }

  private def executeExists(r: Exists): Task[Boolean] = {
    val uri = uri"${config.uri}/${r.index}/$Doc/${r.id}".withParams(getQueryParams(List(("routing", r.routing))))

    sendRequest(request.head(uri)).flatMap { response =>
      response.code match {
        case HttpOk       => ZIO.succeed(true)
        case HttpNotFound => ZIO.succeed(false)
        case _            => ZIO.fail(createElasticException(response))
      }
    }
  }

  private def executeGetById(r: GetById): Task[GetResult] = {
    val uri = uri"${config.uri}/${r.index}/$Doc/${r.id}".withParams(getQueryParams(List(("routing", r.routing))))

    sendRequestWithCustomResponse[ElasticGetResponse](
      request
        .get(uri)
        .response(asJson[ElasticGetResponse])
    ).flatMap { response =>
      response.code match {
        case HttpOk       => ZIO.attempt(new GetResult(response.body.toOption.map(d => Document.from(d.source))))
        case HttpNotFound => ZIO.succeed(new GetResult(None))
        case _            => ZIO.fail(createElasticExceptionFromCustomResponse(response))
      }
    }
  }

  private def executeGetByQuery(r: GetByQuery): Task[SearchResult] =
    sendRequestWithCustomResponse(
      request
        .post(uri"${config.uri}/${r.index}/$Search")
        .response(asJson[ElasticQueryResponse])
        .contentType(ApplicationJson)
        .body(r.query.toJson)
    ).flatMap { response =>
      response.code match {
        case HttpOk =>
          response.body.fold(
            e => ZIO.fail(new ElasticException(s"Exception occurred: ${e.getMessage}")),
            value =>
              ZIO.succeed(
                new SearchResult(value.results.map(_.toString).map(Document(_)))
              ) // TODO have Json in Document instead of String???
          )
        case _ =>
          ZIO.fail(createElasticExceptionFromCustomResponse(response))
      }
    }

  private def executeGetByQueryWithScroll(r: GetByQuery): ZIO[Any, Throwable, (Chunk[RawItem], Option[String])] =
    sendRequestWithCustomResponse(
      request
        .post(
          uri"${config.uri}/${r.index}/$Search".withParams(("scroll", "1m"))
        )
        .response(asJson[ElasticQueryResponse])
        .contentType(ApplicationJson)
        .body(r.query.toJson)
    ).flatMap { response =>
      response.code match {
        case HttpOk =>
          response.body.fold(
            e => ZIO.fail(new ElasticException(s"Exception occurred: ${e.getMessage}")),
            value => ZIO.succeed((Chunk.fromIterable(value.results).map(RawItem), value.scrollId))
          )
        case _ =>
          ZIO.fail(createElasticExceptionFromCustomResponse(response))
      }
    }

  private def executeGetByScroll(scrollId: String): ZIO[Any, Throwable, (Chunk[RawItem], Option[String])] =
    sendRequestWithCustomResponse(
      request
        .post(uri"${config.uri}/$Search/scroll".withParams(("scroll", "1m")))
        .response(asJson[ElasticQueryResponse])
        .contentType(ApplicationJson)
        .body(Obj("scroll_id" -> Str(scrollId)))
    ).flatMap { response =>
      response.code match {
        case HttpOk =>
          response.body.fold(
            e => ZIO.fail(new ElasticException(s"Exception occurred: ${e.getMessage}")),
            value =>
              if (value.results.isEmpty) ZIO.succeed((Chunk.empty, None))
              else ZIO.succeed((Chunk.fromIterable(value.results).map(RawItem), Some(scrollId)))
          )
        case _ =>
          ZIO.fail(createElasticExceptionFromCustomResponse(response))
      }
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

}

private[elasticsearch] object HttpElasticExecutor {

  private final val Bulk          = "_bulk"
  private final val Create        = "_create"
  private final val DeleteByQuery = "_delete_by_query"
  private final val Doc           = "_doc"
  private final val Search        = "_search"

  def apply(config: ElasticConfig, client: SttpBackend[Task, Any]) =
    new HttpElasticExecutor(config, client)
}
