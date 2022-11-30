package zio.elasticsearch

import zio.elasticsearch.ElasticError.DocumentRetrievingError._
import zio.elasticsearch.ElasticError._
import zio.schema.Schema
import zio.{RIO, ZIO}

sealed trait ElasticRequest[+A] { self =>
  final def execute: RIO[ElasticExecutor, A] =
    ZIO.serviceWithZIO[ElasticExecutor](_.execute(self))

  final def map[B](f: A => B): ElasticRequest[B] = ElasticRequest.Map(self, f)
}

object ElasticRequest {

  def create[A: Schema](
    index: IndexName,
    id: DocumentId,
    doc: A,
    routing: Option[Routing] = None
  ): ElasticRequest[Unit] =
    Create(index = index, id = Some(id), document = Document.from(doc), routing = routing).map(_ => ())

  def create[A: Schema](
    index: IndexName,
    doc: A,
    routing: Option[Routing]
  ): ElasticRequest[Option[DocumentId]] =
    Create(index = index, id = None, document = Document.from(doc), routing = routing)

  def exists(index: IndexName, id: DocumentId): ElasticRequest[Boolean] = Exists(index = index, id = id)

  def getById[A: Schema](
    index: IndexName,
    id: DocumentId,
    routing: Option[Routing] = None
  ): ElasticRequest[Either[DocumentRetrievingError, A]] =
    GetById(index, id, routing).map {
      case Some(document) => document.decode.left.map(err => DecoderError(err.message))
      case None           => Left(DocumentNotFound)
    }

  def upsert[A: Schema](
    index: IndexName,
    id: DocumentId,
    doc: A,
    routing: Option[Routing] = None
  ): ElasticRequest[Unit] =
    CreateOrUpdate(index = index, id = id, document = Document.from(doc), routing = routing)

  def createIndex(
    name: IndexName,
    definition: Option[String]
  ): ElasticRequest[Unit] = CreateIndex(name, definition)

  private[elasticsearch] final case class Create(
    index: IndexName,
    id: Option[DocumentId],
    document: Document,
    routing: Option[Routing] = None
  ) extends ElasticRequest[Option[DocumentId]]

  private[elasticsearch] final case class CreateOrUpdate(
    index: IndexName,
    id: DocumentId,
    document: Document,
    routing: Option[Routing] = None
  ) extends ElasticRequest[Unit]

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

  private[elasticsearch] final case class CreateIndex(name: IndexName, definition: Option[String])
      extends ElasticRequest[Unit]

  private[elasticsearch] final case class Map[A, B](request: ElasticRequest[A], mapper: A => B)
      extends ElasticRequest[B]
}
