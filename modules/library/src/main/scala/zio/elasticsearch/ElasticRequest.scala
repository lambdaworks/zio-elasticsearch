package zio.elasticsearch

import zio.elasticsearch.ElasticError._
import zio.elasticsearch.ElasticError.DocumentGettingError._
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
  ): ElasticRequest[Either[DocumentGettingError, A]] =
    GetById(index, id, routing).map {
      case Some(document) => document.decode.left.map(err => JsonDecoderError(err.message))
      case None           => Left(DocumentNotFound)
    }

  private[elasticsearch] final case class GetById(
    index: IndexName,
    id: DocumentId,
    routing: Option[Routing] = None
  ) extends ElasticRequest[Option[Document]]

}
