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

import zio.elasticsearch.Refresh.WithRefresh
import zio.elasticsearch.Routing.{Routing, WithRouting}
import zio.prelude._
import zio.schema.Schema
import zio.schema.codec.JsonCodec.JsonDecoder
import zio.{Chunk, RIO, ZIO}

import scala.annotation.unused
import scala.language.implicitConversions

sealed trait ElasticRequest[+A, ERT <: ElasticRequestType] { self =>

  final def execute: RIO[ElasticExecutor, A] =
    ZIO.serviceWithZIO[ElasticExecutor](_.execute(self))

  final def map[B](f: A => Either[DecodingException, B]): ElasticRequest[B, ERT] = ElasticRequest.Map(self, f)

  final def refresh(value: Boolean)(implicit wr: WithRefresh[ERT]): ElasticRequest[A, ERT] =
    wr.withRefresh(request = self, value = value)

  final def refreshFalse(implicit wr: WithRefresh[ERT]): ElasticRequest[A, ERT] =
    wr.withRefresh(request = self, value = false)

  final def refreshTrue(implicit wr: WithRefresh[ERT]): ElasticRequest[A, ERT] =
    wr.withRefresh(request = self, value = true)

  final def routing(value: Routing)(implicit wr: WithRouting[ERT]): ElasticRequest[A, ERT] =
    wr.withRouting(request = self, routing = value)
}

object ElasticRequest {

  import ElasticRequestType._

  def bulk(requests: BulkableRequest*): ElasticRequest[Unit, Bulk] =
    BulkRequest.of(requests: _*)

  def create[A: Schema](index: IndexName, doc: A): ElasticRequest[DocumentId, Create] =
    CreateRequest(index, Document.from(doc))

  def create[A: Schema](index: IndexName, id: DocumentId, doc: A): ElasticRequest[CreationOutcome, CreateWithId] =
    CreateWithIdRequest(index, id, Document.from(doc))

  def createIndex(name: IndexName, definition: Option[String]): ElasticRequest[CreationOutcome, CreateIndex] =
    CreateIndexRequest(name, definition)

  def deleteById(index: IndexName, id: DocumentId): ElasticRequest[DeletionOutcome, DeleteById] =
    DeleteByIdRequest(index, id)

  def deleteByQuery(index: IndexName, query: ElasticQuery[_]): ElasticRequest[DeletionOutcome, DeleteByQuery] =
    DeleteByQueryRequest(index, query)

  def deleteIndex(name: IndexName): ElasticRequest[DeletionOutcome, DeleteIndex] =
    DeleteIndexRequest(name)

  def exists(index: IndexName, id: DocumentId): ElasticRequest[Boolean, Exists] =
    ExistsRequest(index, id)

  def getById[A: Schema](index: IndexName, id: DocumentId): ElasticRequest[Option[A], GetById] =
    GetByIdRequest(index, id).map {
      case Some(document) =>
        document.decode match {
          case Left(e)    => Left(DecodingException(s"Could not parse the document: ${e.message}"))
          case Right(doc) => Right(Some(doc))
        }
      case None =>
        Right(None)
    }

  def search[A](index: IndexName, query: ElasticQuery[_])(implicit
    schema: Schema[A]
  ): ElasticRequest[List[A], GetByQuery] =
    GetByQueryRequest(index, query).map { response =>
      Validation
        .validateAll(response.results.map { json =>
          ZValidation.fromEither(JsonDecoder.decode(schema, json.toString))
        })
        .toEitherWith { errors =>
          DecodingException(s"Could not parse all documents successfully: ${errors.map(_.message).mkString(",")})")
        }
    }

  def upsert[A: Schema](index: IndexName, id: DocumentId, doc: A): ElasticRequest[Unit, Upsert] =
    CreateOrUpdateRequest(index, id, Document.from(doc))

  private[elasticsearch] final case class BulkableRequest private (request: ElasticRequest[_, _])

  object BulkableRequest {
    implicit def toBulkable[ERT <: ElasticRequestType](request: ElasticRequest[_, ERT])(implicit
      @unused ev: ERT <:< BulkableRequestType
    ): BulkableRequest =
      BulkableRequest(request)

    implicit def toBulkableList[ERT <: ElasticRequestType](requests: List[ElasticRequest[_, ERT]])(implicit
      @unused ev: ERT <:< BulkableRequestType
    ): List[BulkableRequest] =
      requests.map(BulkableRequest(_))
  }

  private[elasticsearch] final case class BulkRequest(
    requests: Chunk[BulkableRequest],
    index: Option[IndexName] = None,
    refresh: Boolean = false,
    routing: Option[Routing] = None
  ) extends ElasticRequest[Unit, Bulk] {
    lazy val body: String = requests.flatMap { r =>
      // We use @unchecked to ignore 'pattern match not exhaustive' error since we guarantee that it will not happen
      // because these are only Bulkable Requests and other matches will not occur.
      (r.request: @unchecked) match {
        case CreateRequest(index, document, _, maybeRouting) =>
          List(getActionAndMeta("create", List(("_index", Some(index)), ("routing", maybeRouting))), document.json)
        case CreateWithIdRequest(index, id, document, _, maybeRouting) =>
          List(
            getActionAndMeta("create", List(("_index", Some(index)), ("_id", Some(id)), ("routing", maybeRouting))),
            document.json
          )
        case CreateOrUpdateRequest(index, id, document, _, maybeRouting) =>
          List(
            getActionAndMeta("index", List(("_index", Some(index)), ("_id", Some(id)), ("routing", maybeRouting))),
            document.json
          )
        case DeleteByIdRequest(index, id, _, maybeRouting) =>
          List(getActionAndMeta("delete", List(("_index", Some(index)), ("_id", Some(id)), ("routing", maybeRouting))))
      }
    }.mkString(start = "", sep = "\n", end = "\n")
  }

  object BulkRequest {
    def of(requests: BulkableRequest*): BulkRequest = BulkRequest(Chunk.fromIterable(requests))
  }

  private[elasticsearch] final case class CreateRequest(
    index: IndexName,
    document: Document,
    refresh: Boolean = false,
    routing: Option[Routing] = None
  ) extends ElasticRequest[DocumentId, Create]

  private[elasticsearch] final case class CreateWithIdRequest(
    index: IndexName,
    id: DocumentId,
    document: Document,
    refresh: Boolean = false,
    routing: Option[Routing] = None
  ) extends ElasticRequest[CreationOutcome, CreateWithId]

  private[elasticsearch] final case class CreateIndexRequest(
    name: IndexName,
    definition: Option[String]
  ) extends ElasticRequest[CreationOutcome, CreateIndex]

  private[elasticsearch] final case class CreateOrUpdateRequest(
    index: IndexName,
    id: DocumentId,
    document: Document,
    refresh: Boolean = false,
    routing: Option[Routing] = None
  ) extends ElasticRequest[Unit, Upsert]

  private[elasticsearch] final case class DeleteByIdRequest(
    index: IndexName,
    id: DocumentId,
    refresh: Boolean = false,
    routing: Option[Routing] = None
  ) extends ElasticRequest[DeletionOutcome, DeleteById]

  private[elasticsearch] final case class DeleteByQueryRequest(
    index: IndexName,
    query: ElasticQuery[_],
    refresh: Boolean = false,
    routing: Option[Routing] = None
  ) extends ElasticRequest[DeletionOutcome, DeleteByQuery]

  private[elasticsearch] final case class DeleteIndexRequest(name: IndexName)
      extends ElasticRequest[DeletionOutcome, DeleteIndex]

  private[elasticsearch] final case class ExistsRequest(
    index: IndexName,
    id: DocumentId,
    routing: Option[Routing] = None
  ) extends ElasticRequest[Boolean, Exists]

  private[elasticsearch] final case class GetByIdRequest(
    index: IndexName,
    id: DocumentId,
    routing: Option[Routing] = None
  ) extends ElasticRequest[Option[Document], GetById]

  private[elasticsearch] final case class GetByQueryRequest(
    index: IndexName,
    query: ElasticQuery[_],
    routing: Option[Routing] = None
  ) extends ElasticRequest[ElasticQueryResponse, GetByQuery]

  private[elasticsearch] final case class Map[A, B, ERT <: ElasticRequestType](
    request: ElasticRequest[A, ERT],
    mapper: A => Either[DecodingException, B]
  ) extends ElasticRequest[B, ERT]

  private def getActionAndMeta(requestType: String, parameters: List[(String, Any)]): String =
    parameters.collect { case (name, Some(value)) => s""""$name" : "${value.toString}"""" }
      .mkString(s"""{ "$requestType" : { """, ", ", " } }")

}

sealed trait ElasticRequestType

sealed trait BulkableRequestType extends ElasticRequestType

object ElasticRequestType {
  sealed trait Bulk          extends ElasticRequestType
  sealed trait CreateIndex   extends ElasticRequestType
  sealed trait Create        extends BulkableRequestType
  sealed trait CreateWithId  extends BulkableRequestType
  sealed trait DeleteById    extends BulkableRequestType
  sealed trait DeleteByQuery extends ElasticRequestType
  sealed trait DeleteIndex   extends ElasticRequestType
  sealed trait Exists        extends ElasticRequestType
  sealed trait GetById       extends ElasticRequestType
  sealed trait GetByQuery    extends ElasticRequestType
  sealed trait Upsert        extends BulkableRequestType
}

sealed abstract class CreationOutcome

case object Created       extends CreationOutcome
case object AlreadyExists extends CreationOutcome

sealed abstract class DeletionOutcome

case object Deleted  extends DeletionOutcome
case object NotFound extends DeletionOutcome
