package zio.elasticsearch

import zio.elasticsearch.ElasticError.DocumentRetrievingError._
import zio.elasticsearch.ElasticError._
import zio.elasticsearch.ElasticRequest.AddRouting
import zio.elasticsearch.Refresh.WithRefresh
import zio.schema.Schema
import zio.{RIO, ZIO}

sealed trait ElasticRequest[+A, ERT <: ElasticRequestType] { self =>

  final def execute: RIO[ElasticExecutor, A] =
    ZIO.serviceWithZIO[ElasticExecutor](_.execute(self))

  final def map[B](f: A => B): ElasticRequest[B, ERT] = ElasticRequest.Map(self, f)

  final def refresh(value: Boolean)(implicit wr: WithRefresh[ERT]): ElasticRequest[A, ERT] =
    wr.withRefresh(request = self, value = value)

  final def refreshFalse(implicit wr: WithRefresh[ERT]): ElasticRequest[A, ERT] =
    wr.withRefresh(request = self, value = false)

  final def refreshTrue(implicit wr: WithRefresh[ERT]): ElasticRequest[A, ERT] =
    wr.withRefresh(request = self, value = true)

  final def routing(value: Routing)(implicit addRouting: AddRouting[ERT]): ElasticRequest[A, ERT] =
    addRouting.addRouting(self, value)
}

object ElasticRequest {

  import zio.elasticsearch.ElasticRequestType._

  def create[A: Schema](index: IndexName, id: DocumentId, doc: A): ElasticRequest[Unit, Create] =
    CreateRequest(index, Some(id), Document.from(doc)).map(_ => ())

  def create[A: Schema](index: IndexName, doc: A): ElasticRequest[Option[DocumentId], Create] =
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
  ) extends ElasticRequest[Option[DocumentId], Create]

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

  trait AddRouting[T <: ElasticRequestType] {
    def addRouting[A](req: ElasticRequest[A, T], routing: Routing): ElasticRequest[A, T]
  }

  object AddRouting {
    implicit val addRoutingToCreate: AddRouting[Create] = new AddRouting[Create] {
      override def addRouting[A](req: ElasticRequest[A, Create], routing: Routing): ElasticRequest[A, Create] =
        req match {
          case Map(r, mapper)   => Map(addRouting(r, routing), mapper)
          case r: CreateRequest => r.copy(routing = Some(routing))
        }
    }
    implicit val addRoutingToDeleteById: AddRouting[DeleteById] = new AddRouting[DeleteById] {
      override def addRouting[A](
        req: ElasticRequest[A, DeleteById],
        routing: Routing
      ): ElasticRequest[A, DeleteById] =
        req match {
          case Map(r, mapper)       => Map(addRouting(r, routing), mapper)
          case r: DeleteByIdRequest => r.copy(routing = Some(routing))
        }
    }
    implicit val addRoutingToExists: AddRouting[Exists] = new AddRouting[Exists] {
      override def addRouting[A](req: ElasticRequest[A, Exists], routing: Routing): ElasticRequest[A, Exists] =
        req match {
          case Map(r, mapper)   => Map(addRouting(r, routing), mapper)
          case r: ExistsRequest => r.copy(routing = Some(routing))
        }
    }
    implicit val addRoutingToGetById: AddRouting[GetById] = new AddRouting[GetById] {
      override def addRouting[A](
        req: ElasticRequest[A, GetById],
        routing: Routing
      ): ElasticRequest[A, GetById] =
        req match {
          case Map(r, mapper)    => Map(addRouting(r, routing), mapper)
          case r: GetByIdRequest => r.copy(routing = Some(routing))
        }
    }
    implicit val addRoutingToUpsert: AddRouting[Upsert] = new AddRouting[Upsert] {
      override def addRouting[A](req: ElasticRequest[A, Upsert], routing: Routing): ElasticRequest[A, Upsert] =
        req match {
          case Map(r, mapper)           => Map(addRouting(r, routing), mapper)
          case r: CreateOrUpdateRequest => r.copy(routing = Some(routing))
        }
    }
  }

}

sealed trait ElasticRequestType

object ElasticRequestType {
  trait CreateIndex extends ElasticRequestType
  trait Create      extends ElasticRequestType
  trait DeleteById  extends ElasticRequestType
  trait DeleteIndex extends ElasticRequestType
  trait Exists      extends ElasticRequestType
  trait GetById     extends ElasticRequestType
  trait GetByQuery  extends ElasticRequestType
  trait Upsert      extends ElasticRequestType
}
