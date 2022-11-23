package zio.elasticsearch

import zio.elasticsearch.ElasticRequest.DocumentGettingError.{DocumentNotFound, JsonDecoderError}
import zio.schema.Schema

sealed trait ElasticRequest[+A] { self =>
  final def map[B](f: A => B): ElasticRequest[B] = ElasticRequest.Map(self, f)

}

object ElasticRequest {

  private[elasticsearch] final case class Map[A, B](request: ElasticRequest[A], mapper: A => B)
      extends ElasticRequest[B]

  def getById[A: Schema](
    index: Index,
    id: DocumentId,
    routing: Option[Routing] = None
  ): ElasticRequest[Either[DocumentGettingError, A]] =
    GetById(index, id, routing).map {
      case Some(document) => document.decode.left.map(err => JsonDecoderError(err.message))
      case None           => Left(DocumentNotFound)
    }

  private[elasticsearch] final case class GetById(
    index: Index,
    id: DocumentId,
    routing: Option[Routing] = None
  ) extends ElasticRequest[Option[Document]]

  sealed abstract class DocumentGettingError

  object DocumentGettingError {

    case object DocumentNotFound extends DocumentGettingError

    case class JsonDecoderError(errorMsg: String) extends DocumentGettingError

  }

}
