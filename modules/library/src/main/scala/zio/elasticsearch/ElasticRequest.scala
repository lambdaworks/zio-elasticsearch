package zio.elasticsearch

import zio.elasticsearch.ElasticError.DocumentRetrievingError._
import zio.elasticsearch.ElasticError._
import zio.elasticsearch.ElasticRequest._
import zio.schema.Schema
import zio.{RIO, ZIO}

sealed trait ElasticRequest[+A] { self =>
  final def execute: RIO[ElasticExecutor, A] =
    ZIO.serviceWithZIO[ElasticExecutor](_.execute(self))

  final def map[B](f: A => B): ElasticRequest[B] = ElasticRequest.Map(self, f)

  final def routing(value: Routing): ElasticRequest[A] =
    self match {
      case Map(request, mapper) => Map(request.routing(value), mapper)
      case r: Create            => r.copy(routing = Some(value)).asInstanceOf[ElasticRequest[A]]
      case r: CreateOrUpdate    => r.copy(routing = Some(value)).asInstanceOf[ElasticRequest[A]]
      case r: DeleteById        => r.copy(routing = Some(value)).asInstanceOf[ElasticRequest[A]]
      case r: Exists            => r.copy(routing = Some(value)).asInstanceOf[ElasticRequest[A]]
      case r: GetById           => r.copy(routing = Some(value)).asInstanceOf[ElasticRequest[A]]
      case _                    => self
    }
}

object ElasticRequest {

  def create[A: Schema](index: IndexName, id: DocumentId, doc: A): ElasticRequest[Unit] =
    Create(index, Some(id), Document.from(doc)).map(_ => ())

  def create[A: Schema](index: IndexName, doc: A): ElasticRequest[Option[DocumentId]] =
    Create(index, None, Document.from(doc))

  def deleteById(index: IndexName, id: DocumentId): ElasticRequest[Either[DocumentNotFound.type, Unit]] =
    DeleteById(index, id).map(_.toRight(DocumentNotFound))

  def exists(index: IndexName, id: DocumentId): ElasticRequest[Boolean] =
    Exists(index, id)

  def getById[A: Schema](index: IndexName, id: DocumentId): ElasticRequest[Either[DocumentRetrievingError, A]] =
    GetById(index, id).map {
      case Some(document) => document.decode.left.map(err => DecoderError(err.message))
      case None           => Left(DocumentNotFound)
    }

  def query(index: IndexName, query: ElasticQuery): ElasticRequest[Unit] =
    Query(index, query)

  def createIndex(name: IndexName, definition: Option[String]): ElasticRequest[Unit] =
    CreateIndex(name, definition)

  def deleteIndex(name: IndexName): ElasticRequest[Unit] =
    DeleteIndex(name)

  def upsert[A: Schema](index: IndexName, id: DocumentId, doc: A): ElasticRequest[Unit] =
    CreateOrUpdate(index, id, Document.from(doc))

  private[elasticsearch] final case class Create(
    index: IndexName,
    id: Option[DocumentId],
    document: Document,
    routing: Option[Routing] = None
  ) extends ElasticRequest[Option[DocumentId]]

  private[elasticsearch] final case class CreateIndex(
    name: IndexName,
    definition: Option[String]
  ) extends ElasticRequest[Unit]

  private[elasticsearch] final case class CreateOrUpdate(
    index: IndexName,
    id: DocumentId,
    document: Document,
    routing: Option[Routing] = None
  ) extends ElasticRequest[Unit]

  private[elasticsearch] final case class DeleteById(
    index: IndexName,
    id: DocumentId,
    routing: Option[Routing] = None
  ) extends ElasticRequest[Option[Unit]]

  private[elasticsearch] final case class DeleteIndex(name: IndexName) extends ElasticRequest[Unit]

  private[elasticsearch] final case class Exists(
    index: IndexName,
    id: DocumentId,
    routing: Option[Routing] = None
  ) extends ElasticRequest[Boolean]

  private[elasticsearch] final case class GetById(
    index: IndexName,
    id: DocumentId,
    routing: Option[Routing] = None
  ) extends ElasticRequest[Option[Document]]

  private[elasticsearch] final case class Query(
    index: IndexName,
    query: ElasticQuery,
    routing: Option[Routing] = None
  ) extends ElasticRequest[Unit]

  private[elasticsearch] final case class Map[A, B](request: ElasticRequest[A], mapper: A => B)
      extends ElasticRequest[B]
}
