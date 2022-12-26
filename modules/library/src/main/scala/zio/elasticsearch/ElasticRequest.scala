package zio.elasticsearch

import zio.elasticsearch.ElasticError.DocumentRetrievingError._
import zio.elasticsearch.ElasticError._
import zio.elasticsearch.ElasticRequest._
import zio.schema.Schema
import zio.{RIO, ZIO}

sealed trait ElasticRequest[+A, ERT <: ElasticRequestType] { self =>

  final def execute: RIO[ElasticExecutor, A] =
    ZIO.serviceWithZIO[ElasticExecutor](_.execute(self))

  final def map[B](f: A => B): ElasticRequest[B, ERT] = ElasticRequest.Map(self, f)

  final def refresh()(implicit request: WithRefresh[ERT]): ElasticRequest[A, ERT] = request.withRefresh(self)

  final def routing(value: Routing): ElasticRequest[A, ERT] = self match {
    case Map(request, mapper) => Map(request.routing(value), mapper)
    case r: Create            => r.copy(routing = Some(value)).asInstanceOf[ElasticRequest[A, ERT]]
    case r: CreateOrUpdate    => r.copy(routing = Some(value)).asInstanceOf[ElasticRequest[A, ERT]]
    case r: DeleteById        => r.copy(routing = Some(value)).asInstanceOf[ElasticRequest[A, ERT]]
    case r: Exists            => r.copy(routing = Some(value)).asInstanceOf[ElasticRequest[A, ERT]]
    case r: GetById           => r.copy(routing = Some(value)).asInstanceOf[ElasticRequest[A, ERT]]
    case _                    => self
  }
}

object ElasticRequest {

  import zio.elasticsearch.ElasticRequestType._

  def create[A: Schema](index: IndexName, id: DocumentId, doc: A): ElasticRequest[Unit, CreateType] =
    Create(index, Some(id), Document.from(doc)).map(_ => ())

  def create[A: Schema](index: IndexName, doc: A): ElasticRequest[Option[DocumentId], CreateType] =
    Create(index, None, Document.from(doc))

  def createIndex(name: IndexName, definition: Option[String]): ElasticRequest[Unit, CreateIndexType] =
    CreateIndex(name, definition)

  def deleteById(
    index: IndexName,
    id: DocumentId
  ): ElasticRequest[Either[DocumentNotFound.type, Unit], DeleteByIdType] =
    DeleteById(index, id).map(deleted => if (deleted) Right(()) else Left(DocumentNotFound))

  def deleteIndex(name: IndexName): ElasticRequest[Boolean, DeleteIndexType] =
    DeleteIndex(name)

  def exists(index: IndexName, id: DocumentId): ElasticRequest[Boolean, ExistsType] =
    Exists(index, id)

  def getById[A: Schema](
    index: IndexName,
    id: DocumentId
  ): ElasticRequest[Either[DocumentRetrievingError, A], GetByIdType] =
    GetById(index, id).map {
      case Some(document) => document.decode.left.map(err => DecoderError(err.message))
      case None           => Left(DocumentNotFound)
    }

  def search(index: IndexName, query: ElasticQuery): ElasticRequest[Option[ElasticQueryResponse], GetByQueryType] =
    GetByQuery(index, query)

  def upsert[A: Schema](index: IndexName, id: DocumentId, doc: A): ElasticRequest[Unit, UpsertType] =
    CreateOrUpdate(index, id, Document.from(doc))

  private[elasticsearch] final case class Create(
    index: IndexName,
    id: Option[DocumentId],
    document: Document,
    refresh: Boolean = false,
    routing: Option[Routing] = None
  ) extends ElasticRequest[Option[DocumentId], CreateType]

  private[elasticsearch] final case class CreateIndex(
    name: IndexName,
    definition: Option[String]
  ) extends ElasticRequest[Unit, CreateIndexType]

  private[elasticsearch] final case class CreateOrUpdate(
    index: IndexName,
    id: DocumentId,
    document: Document,
    refresh: Boolean = false,
    routing: Option[Routing] = None
  ) extends ElasticRequest[Unit, UpsertType]

  private[elasticsearch] final case class DeleteById(
    index: IndexName,
    id: DocumentId,
    refresh: Boolean = false,
    routing: Option[Routing] = None
  ) extends ElasticRequest[Boolean, DeleteByIdType]

  private[elasticsearch] final case class DeleteIndex(name: IndexName) extends ElasticRequest[Boolean, DeleteIndexType]

  private[elasticsearch] final case class Exists(
    index: IndexName,
    id: DocumentId,
    routing: Option[Routing] = None
  ) extends ElasticRequest[Boolean, ExistsType]

  private[elasticsearch] final case class GetById(
    index: IndexName,
    id: DocumentId,
    routing: Option[Routing] = None
  ) extends ElasticRequest[Option[Document], GetByIdType]

  private[elasticsearch] final case class GetByQuery(
    index: IndexName,
    query: ElasticQuery,
    routing: Option[Routing] = None
  ) extends ElasticRequest[Option[ElasticQueryResponse], GetByQueryType]

  private[elasticsearch] final case class Map[A, B, ERT <: ElasticRequestType](
    request: ElasticRequest[A, ERT],
    mapper: A => B
  ) extends ElasticRequest[B, ERT]

  trait WithRefresh[ERT <: ElasticRequestType] {
    def withRefresh[A](request: ElasticRequest[A, ERT]): ElasticRequest[A, ERT]
  }

  object WithRefresh {
    implicit val createWithRefresh: WithRefresh[CreateType] = new WithRefresh[CreateType] {
      override def withRefresh[A](request: ElasticRequest[A, CreateType]): ElasticRequest[A, CreateType] =
        request match {
          case Map(r, mapper) => Map(withRefresh(r), mapper)
          case r: Create      => r.copy(refresh = true)
        }
    }
    implicit val upsertWithRefresh: WithRefresh[UpsertType] = new WithRefresh[UpsertType] {
      override def withRefresh[A](request: ElasticRequest[A, UpsertType]): ElasticRequest[A, UpsertType] =
        request match {
          case Map(r, mapper)    => Map(withRefresh(r), mapper)
          case r: CreateOrUpdate => r.copy(refresh = true)
        }
    }

    implicit val deleteByIdWithRefresh: WithRefresh[DeleteByIdType] = new WithRefresh[DeleteByIdType] {
      override def withRefresh[A](request: ElasticRequest[A, DeleteByIdType]): ElasticRequest[A, DeleteByIdType] =
        request match {
          case Map(r, mapper) => Map(withRefresh(r), mapper)
          case r: DeleteById  => r.copy(refresh = true)
        }
    }
  }
}

sealed trait ElasticRequestType

object ElasticRequestType {
  trait CreateIndexType extends ElasticRequestType
  trait CreateType      extends ElasticRequestType
  trait DeleteByIdType  extends ElasticRequestType
  trait DeleteIndexType extends ElasticRequestType
  trait ExistsType      extends ElasticRequestType
  trait GetByIdType     extends ElasticRequestType
  trait GetByQueryType  extends ElasticRequestType
  trait UpsertType      extends ElasticRequestType
}
