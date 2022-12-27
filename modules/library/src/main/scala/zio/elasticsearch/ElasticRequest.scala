package zio.elasticsearch

import zio.elasticsearch.ElasticError.DocumentRetrievingError._
import zio.elasticsearch.ElasticError._
import zio.elasticsearch.ElasticRequest._
import zio.elasticsearch.Refresh.WithRefresh
import zio.schema.Schema
import zio.{RIO, ZIO}

sealed trait ElasticRequest[+A, ERT <: ElasticRequestType] { self =>

  final def execute: RIO[ElasticExecutor, A] =
    ZIO.serviceWithZIO[ElasticExecutor](_.execute(self))

  final def map[B](f: A => B): ElasticRequest[B, ERT] = ElasticRequest.Map(self, f)

  final def refresh(value: Boolean)(implicit wr: WithRefresh[ERT]): ElasticRequest[A, ERT] =
    wr.withRefresh(request = self, value = value)

  final def refreshTrue(implicit wr: WithRefresh[ERT]): ElasticRequest[A, ERT] =
    wr.withRefresh(request = self, value = true)

  final def refreshFalse(implicit wr: WithRefresh[ERT]): ElasticRequest[A, ERT] =
    wr.withRefresh(request = self, value = false)

  final def routing(value: Routing): ElasticRequest[A, ERT] = self match {
    case Map(request, mapper)     => Map(request.routing(value), mapper)
    case r: CreateRequest         => r.copy(routing = Some(value)).asInstanceOf[ElasticRequest[A, ERT]]
    case r: CreateOrUpdateRequest => r.copy(routing = Some(value)).asInstanceOf[ElasticRequest[A, ERT]]
    case r: DeleteByIdRequest     => r.copy(routing = Some(value)).asInstanceOf[ElasticRequest[A, ERT]]
    case r: ExistsRequest         => r.copy(routing = Some(value)).asInstanceOf[ElasticRequest[A, ERT]]
    case r: GetByIdRequest        => r.copy(routing = Some(value)).asInstanceOf[ElasticRequest[A, ERT]]
    case _                        => self
  }
}

object ElasticRequest {

  import zio.elasticsearch.ElasticRequestType._

  def create[A: Schema](index: IndexName, id: DocumentId, doc: A): ElasticRequest[Unit, CreateType] =
    CreateRequest(index, Some(id), Document.from(doc)).map(_ => ())

  def create[A: Schema](index: IndexName, doc: A): ElasticRequest[Option[DocumentId], CreateType] =
    CreateRequest(index, None, Document.from(doc))

  def createIndex(name: IndexName, definition: Option[String]): ElasticRequest[Unit, CreateIndex] =
    CreateIndexRequest(name, definition)

  def deleteById(
    index: IndexName,
    id: DocumentId
  ): ElasticRequest[Either[DocumentNotFound.type, Unit], DeleteById] =
    DeleteByIdRequest(index, id).map(deleted => if (deleted) Right(()) else Left(DocumentNotFound))

  def deleteIndex(name: IndexName): ElasticRequest[Boolean, DeleteIndex] =
    DeleteIndexRequest(name)

  def exists(index: IndexName, id: DocumentId): ElasticRequest[Boolean, Exists] =
    ExistsRequest(index, id)

  def getById[A: Schema](
    index: IndexName,
    id: DocumentId
  ): ElasticRequest[Either[DocumentRetrievingError, A], GetById] =
    GetByIdRequest(index, id).map {
      case Some(document) => document.decode.left.map(err => DecoderError(err.message))
      case None           => Left(DocumentNotFound)
    }

  def search(index: IndexName, query: ElasticQuery): ElasticRequest[Option[ElasticQueryResponse], GetByQuery] =
    GetByQueryRequest(index, query)

  def upsert[A: Schema](index: IndexName, id: DocumentId, doc: A): ElasticRequest[Unit, Upsert] =
    CreateOrUpdateRequest(index, id, Document.from(doc))

  private[elasticsearch] final case class CreateRequest(
    index: IndexName,
    id: Option[DocumentId],
    document: Document,
    refresh: Boolean = false,
    routing: Option[Routing] = None
  ) extends ElasticRequest[Option[DocumentId], CreateType]

  private[elasticsearch] final case class CreateIndexRequest(
    name: IndexName,
    definition: Option[String]
  ) extends ElasticRequest[Unit, CreateIndex]

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
  ) extends ElasticRequest[Boolean, DeleteById]

  private[elasticsearch] final case class DeleteIndexRequest(name: IndexName)
      extends ElasticRequest[Boolean, DeleteIndex]

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
  ) extends ElasticRequest[Option[ElasticQueryResponse], GetByQuery]

  private[elasticsearch] final case class Map[A, B, ERT <: ElasticRequestType](
    request: ElasticRequest[A, ERT],
    mapper: A => B
  ) extends ElasticRequest[B, ERT]

}

sealed trait ElasticRequestType

object ElasticRequestType {
  trait CreateIndex extends ElasticRequestType
  trait CreateType  extends ElasticRequestType
  trait DeleteById  extends ElasticRequestType
  trait DeleteIndex extends ElasticRequestType
  trait Exists      extends ElasticRequestType
  trait GetById     extends ElasticRequestType
  trait GetByQuery  extends ElasticRequestType
  trait Upsert      extends ElasticRequestType
}
