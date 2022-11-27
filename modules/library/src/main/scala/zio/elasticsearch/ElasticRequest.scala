package zio.elasticsearch

import zio.elasticsearch.ElasticError.DocumentRetrievingError._
import zio.elasticsearch.ElasticError._
import zio.schema.Schema

sealed trait ElasticRequest[+A] { self =>
  final def map[B](f: A => B): ElasticRequest[B] = ElasticRequest.Map(self, f)

}

object ElasticRequest {

  private[elasticsearch] final case class Map[A, B](request: ElasticRequest[A], mapper: A => B)
      extends ElasticRequest[B]

  def getById[A: Schema](
    index: IndexName,
    id: DocumentId,
    routing: Option[Routing] = None
  ): ElasticRequest[Either[DocumentRetrievingError, A]] =
    GetById(index, id, routing).map {
      case Some(document) => document.decode.left.map(err => DecoderError(err.message))
      case None           => Left(DocumentNotFound)
    }

  def create[A: Schema](
    index: IndexName,
    id: Option[DocumentId],
    doc: A,
    routing: Option[Routing] = None
  ): ElasticRequest[Unit] =
    Create(index, id, Document.from(doc), routing)

  def upsert[A: Schema](
    index: IndexName,
    id: DocumentId,
    doc: A,
    routing: Option[Routing] = None
  ): ElasticRequest[Unit] =
    CreateOrUpdate(index, id, Document.from(doc), routing)

  private[elasticsearch] final case class GetById(
    index: IndexName,
    id: DocumentId,
    routing: Option[Routing] = None
  ) extends ElasticRequest[Option[Document]]

  private[elasticsearch] final case class Create(
    index: IndexName,
    id: Option[DocumentId],
    document: Document,
    routing: Option[Routing] = None
  ) extends ElasticRequest[Unit]

  private[elasticsearch] final case class CreateOrUpdate(
    index: IndexName,
    id: DocumentId,
    document: Document,
    routing: Option[Routing] = None
  ) extends ElasticRequest[Unit]

}
