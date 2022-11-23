package zio.elasticsearch

<<<<<<< HEAD
import zio.elasticsearch.ElasticError.DocumentRetrievingError._
import zio.elasticsearch.ElasticError._
=======
import zio.elasticsearch.ElasticRequest.DocumentGettingError.{DocumentNotFound, JsonDecoderError}
>>>>>>> 9e9aaf4 (Refactor ElasticRequest)
import zio.schema.Schema

sealed trait ElasticRequest[+A] { self =>
  final def map[B](f: A => B): ElasticRequest[B] = ElasticRequest.Map(self, f)

}

object ElasticRequest {

  private[elasticsearch] final case class Map[A, B](request: ElasticRequest[A], mapper: A => B)
      extends ElasticRequest[B]

  def getById[A: Schema](
<<<<<<< HEAD
    index: IndexName,
    id: DocumentId,
    routing: Option[Routing] = None
  ): ElasticRequest[Either[DocumentRetrievingError, A]] =
    GetById(index, id, routing).map {
      case Some(document) => document.decode.left.map(err => DecoderError(err.message))
=======
    index: Index,
    id: DocumentId,
    routing: Option[Routing] = None
  ): ElasticRequest[Either[DocumentGettingError, A]] =
    GetById(index, id, routing).map {
      case Some(document) => document.decode.fold(_ => Left(JsonDecoderError), Right(_))
>>>>>>> 9e9aaf4 (Refactor ElasticRequest)
      case None           => Left(DocumentNotFound)
    }

  private[elasticsearch] final case class GetById(
    index: IndexName,
    id: DocumentId,
    routing: Option[Routing] = None
  ) extends ElasticRequest[Option[Document]]

  sealed abstract class DocumentGettingError

  object DocumentGettingError {

    case object DocumentNotFound extends DocumentGettingError

    case object JsonDecoderError extends DocumentGettingError

  }

}
