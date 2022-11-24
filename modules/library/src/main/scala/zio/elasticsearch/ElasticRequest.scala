package zio.elasticsearch

import zio.elasticsearch.ElasticError.DocumentRetrievingError._
import zio.elasticsearch.ElasticError._
import zio.schema.Schema

sealed trait ElasticRequest[+A] { self =>
  final def map[B](f: A => B): ElasticRequest[B] = ElasticRequest.Map(self, f)
}

object ElasticRequest {

  def create[A: Schema](
    index: IndexName,
    id: DocumentId,
    doc: A,
    routing: Option[Routing] = None
  ): ElasticRequest[Unit] =
    Create(index, Some(id), Document.from(doc), routing)

  def create[A: Schema](
    index: IndexName,
    doc: A,
    routing: Option[Routing]
  ): ElasticRequest[Unit] =
    Create(index, None, Document.from(doc), routing)

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
    CreateOrUpdate(index, id, Document.from(doc), routing)

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

  private[elasticsearch] final case class GetById(
    index: IndexName,
    id: DocumentId,
    routing: Option[Routing] = None
  ) extends ElasticRequest[Option[Document]]

  sealed abstract class DocumentGettingError

  object DocumentGettingError {

    case object DocumentNotFound extends DocumentGettingError

    case class JsonDecoderError(errorMsg: String) extends DocumentGettingError

  }
}
