package zio.elasticsearch

import zio.elasticsearch.ElasticError.DocumentRetrievingError._
import zio.elasticsearch.ElasticError._
import zio.elasticsearch.ElasticRequest._
import zio.schema.Schema
import zio.{RIO, ZIO}

sealed trait ElasticRequestType
object ElasticRequestType {
  trait GetByIdType extends ElasticRequestType
  trait CreateType extends ElasticRequestType
  trait UpsertType extends ElasticRequestType
}

sealed trait ElasticRequest[+A, RequestType <: ElasticRequestType] { self =>

  final def execute: RIO[ElasticExecutor, A] =
    ZIO.serviceWithZIO[ElasticExecutor](_.execute(self))

  final def map[B](f: A => B): ElasticRequest[B, RequestType] = ElasticRequest.Map(self, f)

  final def routing(value: String)(implicit addRouting: AddRouting[RequestType]): ElasticRequest[A, RequestType] =
    addRouting.addRouting(self, value)

  final def refresh(implicit addRefresh: AddRefresh[RequestType]): ElasticRequest[A, RequestType] =
    addRefresh.addRefresh(self)
}

object ElasticRequest {

  import ElasticRequestType._

  def create[A: Schema](index: IndexName, id: DocumentId, doc: A): ElasticRequest[Unit, CreateType] =
    Create(index, Some(id), Document.from(doc)).map(_ => ())

  def create[A: Schema](index: IndexName, doc: A): ElasticRequest[Option[DocumentId], CreateType] =
    Create(index, None, Document.from(doc))

  def getById[A: Schema](index: IndexName, id: DocumentId): ElasticRequest[Either[DocumentRetrievingError, A], GetByIdType] =
    GetById(index, id).map {
      case Some(document) => document.decode.left.map(err => DecoderError(err.message))
      case None           => Left(DocumentNotFound)
    }

  def upsert[A: Schema](index: IndexName, id: DocumentId, doc: A): ElasticRequest[Unit, UpsertType] =
    CreateOrUpdate(index, id, Document.from(doc))

  private[elasticsearch] final case class Create(
    index: IndexName,
    id: Option[DocumentId],
    document: Document,
    routing: Option[Routing] = None,
    refresh: Boolean = false
  ) extends ElasticRequest[Option[DocumentId], CreateType]

  private[elasticsearch] final case class CreateOrUpdate(
    index: IndexName,
    id: DocumentId,
    document: Document,
    routing: Option[Routing] = None,
    refresh: Boolean = false
  ) extends ElasticRequest[Unit, UpsertType]

  private[elasticsearch] final case class GetById(
    index: IndexName,
    id: DocumentId,
    routing: Option[Routing] = None
  ) extends ElasticRequest[Option[Document], GetByIdType]

  private[elasticsearch] final case class Map[A, B, T <: ElasticRequestType](request: ElasticRequest[A, T], mapper: A => B)
      extends ElasticRequest[B, T]

  trait AddRouting[T <: ElasticRequestType] {
    def addRouting[A](req: ElasticRequest[A, T], routing: String): ElasticRequest[A, T]
  }

  object AddRouting {
    implicit val addRoutingToGetById: AddRouting[GetByIdType] = new AddRouting[GetByIdType] {
      override def addRouting[A](req: ElasticRequest[A, GetByIdType], routing: String) = {
        req match {
          case Map(r, mapper) => Map(addRouting(r, routing), mapper)
          case r: GetById => r.copy(routing = Some(Routing(routing)))
        }

      }
    }
    implicit val addRoutingToCreate: AddRouting[CreateType] = new AddRouting[CreateType] {
      override def addRouting[A](req: ElasticRequest[A, CreateType], routing: String) =
        req match {
          case Map(r, mapper) => Map(addRouting(r, routing), mapper)
          case r: Create => r.copy(routing = Some(Routing(routing)))
        }
    }
    implicit val addRoutingToUpsert: AddRouting[UpsertType] = new AddRouting[UpsertType] {
      override def addRouting[A](req: ElasticRequest[A, UpsertType], routing: String) =
        req match {
          case Map(r, mapper) => Map(addRouting(r, routing), mapper)
          case r: CreateOrUpdate => r.copy(routing = Some(Routing(routing)))
        }
    }
  }

  trait AddRefresh[T <: ElasticRequestType] {
    def addRefresh[A](req: ElasticRequest[A, T]): ElasticRequest[A, T]
  }

  object AddRefresh {
    implicit val addRefreshToCreate: AddRefresh[CreateType] = new AddRefresh[CreateType] {
      override def addRefresh[A](req: ElasticRequest[A, CreateType]) =
        req match {
          case Map(r, mapper) => Map(addRefresh(r), mapper)
          case r: Create => r.copy(refresh = true)
        }
    }
    implicit val addRefreshToUpsert: AddRefresh[UpsertType] = new AddRefresh[UpsertType] {
      override def addRefresh[A](req: ElasticRequest[A, UpsertType]) =
        req match {
          case Map(r, mapper) => Map(addRefresh(r), mapper)
          case r: CreateOrUpdate => r.copy(refresh = true)
        }
    }
  }

}
