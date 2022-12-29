package zio.elasticsearch

import zio.elasticsearch.Refresh.WithRefresh
import zio.elasticsearch.Routing.{Routing, WithRouting}
import zio.schema.Schema
import zio.{RIO, ZIO}

import scala.util.{Failure, Success, Try}

sealed trait ElasticRequest[+A, ERT <: ElasticRequestType] { self =>

  final def execute: RIO[ElasticExecutor, A] =
    ZIO.serviceWithZIO[ElasticExecutor](_.execute(self))

  final def map[B](f: A => Try[B]): ElasticRequest[B, ERT] = ElasticRequest.Map(self, f)

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

  def create[A: Schema](index: IndexName, doc: A): ElasticRequest[DocumentId, Create] =
    CreateRequest(index, Document.from(doc))

  def create[A: Schema](index: IndexName, id: DocumentId, doc: A): ElasticRequest[CreationOutcome, CreateWithId] =
    CreateWithIdRequest(index, id, Document.from(doc))

  def createIndex(name: IndexName, definition: Option[String]): ElasticRequest[CreationOutcome, CreateIndex] =
    CreateIndexRequest(name, definition)

  def deleteById(index: IndexName, id: DocumentId): ElasticRequest[DeletionOutcome, DeleteById] =
    DeleteByIdRequest(index, id)

  def deleteIndex(name: IndexName): ElasticRequest[DeletionOutcome, DeleteIndex] =
    DeleteIndexRequest(name)

  def exists(index: IndexName, id: DocumentId): ElasticRequest[Boolean, Exists] =
    ExistsRequest(index, id)

  def getById[A: Schema](index: IndexName, id: DocumentId): ElasticRequest[Option[A], GetById] =
    GetByIdRequest(index, id).map {
      case Some(document) =>
        document.decode match {
          case Left(e)    => Failure(DecodingException(s"Decoding error: ${e.message}"))
          case Right(doc) => Success(Some(doc))
        }
      case None => Success(None)
    }

  def search(index: IndexName, query: ElasticQuery): ElasticRequest[ElasticQueryResponse, GetByQuery] =
    GetByQueryRequest(index, query)

  def upsert[A: Schema](index: IndexName, id: DocumentId, doc: A): ElasticRequest[Unit, Upsert] =
    CreateOrUpdateRequest(index, id, Document.from(doc))

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
    query: ElasticQuery,
    routing: Option[Routing] = None
  ) extends ElasticRequest[ElasticQueryResponse, GetByQuery]

  private[elasticsearch] final case class Map[A, B, ERT <: ElasticRequestType](
    request: ElasticRequest[A, ERT],
    mapper: A => Try[B]
  ) extends ElasticRequest[B, ERT]
}

sealed trait ElasticRequestType

object ElasticRequestType {
  trait CreateIndex  extends ElasticRequestType
  trait Create       extends ElasticRequestType
  trait CreateWithId extends ElasticRequestType
  trait DeleteById   extends ElasticRequestType
  trait DeleteIndex  extends ElasticRequestType
  trait Exists       extends ElasticRequestType
  trait GetById      extends ElasticRequestType
  trait GetByQuery   extends ElasticRequestType
  trait Upsert       extends ElasticRequestType
}

sealed abstract class CreationOutcome

object CreationOutcome {
  case object Created       extends CreationOutcome
  case object AlreadyExists extends CreationOutcome
}

sealed abstract class DeletionOutcome

object DeletionOutcome {
  case object Deleted  extends DeletionOutcome
  case object NotFound extends DeletionOutcome
}
