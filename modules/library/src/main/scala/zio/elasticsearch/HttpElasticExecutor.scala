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
import sttp.model.StatusCode.{BadRequest => HttpBadRequest, Conflict => HttpConflict, Created => HttpCreated, NotFound => HttpNotFound, Ok => HttpOk}
import zio.ZIO.logDebug
import zio.elasticsearch.ElasticRequest._
import zio.elasticsearch.ElasticRequestType.{Bulk, Create, CreateIndex, CreateWithId, DeleteById, DeleteByQuery, DeleteIndex, Exists, GetById, GetByQuery, Upsert}
import zio.prelude.{Validation, ZValidation}
import zio.schema.Schema
import zio.schema.codec.JsonCodec.JsonDecoder
import zio.{Task, ZIO}

import scala.collection.immutable.{Map => ScalaMap}

private[elasticsearch] final class HttpElasticExecutor private (config: ElasticConfig, client: SttpBackend[Task, Any])
    extends ElasticExecutor {

  import HttpElasticExecutor._

  def bulk(requests: BulkableRequest*): ElasticRequest[Unit, Bulk] =
    new BulkRequest(requests = requests.toList, index = None, refresh = false, routing = None) {
      def execute: Task[Unit] = {
        val uri = (index match {
          case Some(index) => uri"${config.uri}/$index/$Bulk"
          case None => uri"${config.uri}/$Bulk"
        }).withParams(getQueryParams(List(("refresh", Some(refresh)), ("routing", routing))))

        sendRequest(
          request.post(uri).contentType(ApplicationJson).body(body)
        ).flatMap { response =>
          response.code match {
            case HttpOk => ZIO.unit
            case _ => ZIO.fail(createElasticException(response))
          }
        }
      }
    }

  def create[A: Schema](index: IndexName, doc: A): ElasticRequest[DocumentId, Create] =
    new CreateRequest(index = index, document = Document.from(doc), refresh = false, routing = None) {
      def execute: Task[DocumentId] = {
        val uri = uri"${config.uri}/$index/$Doc"
          .withParams(getQueryParams(List(("refresh", Some(refresh)), ("routing", routing))))

        sendRequestWithCustomResponse[ElasticCreateResponse](
          request
            .post(uri)
            .contentType(ApplicationJson)
            .body(document.json)
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
    }

      def create[A: Schema](index: IndexName, id: DocumentId, doc: A): ElasticRequest[CreationOutcome, CreateWithId] =
        new CreateWithIdRequest(index = index, id = id, document = Document.from(doc), refresh = false, routing = None) {
          def execute: Task[CreationOutcome] = {
            {
              val uri = uri"${config.uri}/$index/$Create/$id"
                .withParams(getQueryParams(List(("refresh", Some(refresh)), ("routing", routing))))

              sendRequest(
                request
                  .post(uri)
                  .contentType(ApplicationJson)
                  .body(document.json)
              ).flatMap { response =>
                response.code match {
                  case HttpCreated => ZIO.succeed(Created)
                  case HttpConflict => ZIO.succeed(AlreadyExists)
                  case _ => ZIO.fail(createElasticException(response))
                }
              }
            }
          }
        }

      def createIndex(name: IndexName, definition: Option[String]): ElasticRequest[CreationOutcome, CreateIndex] =
        new CreateIndexRequest(name, definition) {
          def execute: Task[CreationOutcome] =
            sendRequest(
              request
                .put(uri"${config.uri}/$name")
                .contentType(ApplicationJson)
                .body(definition.getOrElse(""))
            ).flatMap { response =>
              response.code match {
                case HttpOk => ZIO.succeed(Created)
                case HttpBadRequest => ZIO.succeed(AlreadyExists)
                case _ => ZIO.fail(createElasticException(response))
              }
            }
        }

      def deleteById(index: IndexName, id: DocumentId): ElasticRequest[DeletionOutcome, DeleteById] =
        new DeleteByIdRequest(index = index, id = id, refresh = false, routing = None) {
          def execute: Task[DeletionOutcome] = {
            val uri = uri"${config.uri}/$index/$Doc/$id"
              .withParams(getQueryParams(List(("refresh", Some(refresh)), ("routing", routing))))

            sendRequest(request.delete(uri)).flatMap { response =>
              response.code match {
                case HttpOk => ZIO.succeed(Deleted)
                case HttpNotFound => ZIO.succeed(NotFound)
                case _ => ZIO.fail(createElasticException(response))
              }
            }
          }
        }

      def deleteByQuery(index: IndexName, query: ElasticQuery[_]): ElasticRequest[DeletionOutcome, DeleteByQuery] =
        new DeleteByQueryRequest(index = index, query = query, refresh = false, routing = None) {
          def execute: Task[DeletionOutcome] = {
            val uri =
              uri"${config.uri}/$index/$DeleteByQuery".withParams(getQueryParams(List(("refresh", Some(refresh)))))

            sendRequest(
              request
                .post(uri)
                .contentType(ApplicationJson)
                .body(query.toJson)
            ).flatMap { response =>
              response.code match {
                case HttpOk => ZIO.succeed(Deleted)
                case HttpNotFound => ZIO.succeed(NotFound)
                case _ => ZIO.fail(createElasticException(response))
              }
            }
          }
        }

      def deleteIndex(name: IndexName): ElasticRequest[DeletionOutcome, DeleteIndex] =
        new DeleteIndexRequest(name) {
          def execute: Task[DeletionOutcome] =
            sendRequest(request.delete(uri"${config.uri}/$name")).flatMap { response =>
              response.code match {
                case HttpOk => ZIO.succeed(Deleted)
                case HttpNotFound => ZIO.succeed(NotFound)
                case _ => ZIO.fail(createElasticException(response))
              }
            }
        }

      def exists(index: IndexName, id: DocumentId): ElasticRequest[Boolean, Exists] =
        new ExistsRequest(index = index, id = id, routing = None) {
          def execute: Task[Boolean] = {
            val uri = uri"${config.uri}/$index/$Doc/$id".withParams(getQueryParams(List(("routing", routing))))

            sendRequest(request.head(uri)).flatMap { response =>
              response.code match {
                case HttpOk => ZIO.succeed(true)
                case HttpNotFound => ZIO.succeed(false)
                case _ => ZIO.fail(createElasticException(response))
              }
            }
          }
        }

      def getById[A: Schema](index: IndexName, id: DocumentId): ElasticRequest[Option[A], GetById] =
        new GetByIdRequest(index = index, id = id, routing = None) {
          def execute: Task[Option[Document]] = {
            val uri = uri"${config.uri}/$index/$Doc/$id".withParams(getQueryParams(List(("routing", routing))))

            sendRequestWithCustomResponse[ElasticGetResponse](
              request
                .get(uri)
                .response(asJson[ElasticGetResponse])
            ).flatMap { response =>
              response.code match {
                case HttpOk => ZIO.attempt(response.body.toOption.map(d => Document.from(d.source)))
                case HttpNotFound => ZIO.succeed(None)
                case _ => ZIO.fail(createElasticExceptionFromCustomResponse(response))
              }
            }
          }
        }.map {
          case Some(document) =>
            document.decode match {
              case Left(e) => Left(DecodingException(s"Could not parse the document: ${e.message}"))
              case Right(doc) => Right(Some(doc))
            }
          case None =>
            Right(None)
        }

      def search[A](index: IndexName, query: ElasticQuery[_])(implicit schema: Schema[A]): ElasticRequest[List[A], GetByQuery] =
        new GetByQueryRequest(index = index, query = query, routing = None) {
          def execute: Task[ElasticQueryResponse] =
            sendRequestWithCustomResponse(
              request
                .post(uri"${config.uri}/$index/$Search")
                .response(asJson[ElasticQueryResponse])
                .contentType(ApplicationJson)
                .body(query.toJson)
            ).flatMap { response =>
              response.code match {
                case HttpOk =>
                  response.body.fold(
                    e => ZIO.fail(new ElasticException(s"Exception occurred: ${e.getMessage}")),
                    value => ZIO.succeed(value)
                  )
                case _ =>
                  ZIO.fail(createElasticExceptionFromCustomResponse(response))
              }
            }
        }.map { response =>
          Validation
            .validateAll(response.results.map { json =>
              ZValidation.fromEither(JsonDecoder.decode(schema, json.toString))
            })
            .toEitherWith { errors =>
              DecodingException(s"Could not parse all documents successfully: ${errors.map(_.message).mkString(",")})")
            }
        }

      def upsert[A: Schema](index: IndexName, id: DocumentId, doc: A): ElasticRequest[Unit, Upsert] =
        new CreateOrUpdateRequest(index, id, Document.from(doc), refresh = false, routing = None) {
          def execute: Task[Unit] = {
            val uri = uri"${config.uri}/$index/$Doc/$id"
              .withParams(getQueryParams(List(("refresh", Some(refresh)), ("routing", routing))))

            sendRequest(request.put(uri).contentType(ApplicationJson).body(document.json)).flatMap { response =>
              response.code match {
                case HttpOk | HttpCreated => ZIO.unit
                case _ => ZIO.fail(createElasticException(response))
              }
            }
          }
        }

      private def sendRequest(
         req: RequestT[Identity, Either[String, String], Any]
      ): Task[Response[Either[String, String]]] =
        for {
          _ <- logDebug(s"[es-req]: ${req.show(includeBody = true, includeHeaders = true, sensitiveHeaders = Set.empty)}")
          resp <- req.send(client)
          _ <- logDebug(s"[es-res]: ${resp.show(includeBody = true, includeHeaders = true, sensitiveHeaders = Set.empty)}")
        } yield resp

      private def sendRequestWithCustomResponse[A](
        req: RequestT[Identity, Either[ResponseException[String, String], A], Any]
      ): Task[Response[Either[ResponseException[String, String], A]]] =
        for {
          _ <- logDebug(s"[es-req]: ${req.show(includeBody = true, includeHeaders = true, sensitiveHeaders = Set.empty)}")
          resp <- req.send(client)
          _ <- logDebug(s"[es-res]: ${resp.show(includeBody = true, includeHeaders = true, sensitiveHeaders = Set.empty)}")
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

    private final val Bulk = "_bulk"
    private final val Create = "_create"
    private final val DeleteByQuery = "_delete_by_query"
    private final val Doc = "_doc"
    private final val Search = "_search"

    def apply(config: ElasticConfig, client: SttpBackend[Task, Any]) =
      new HttpElasticExecutor(config, client)
  }