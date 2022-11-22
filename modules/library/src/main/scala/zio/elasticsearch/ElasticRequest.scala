package zio.elasticsearch

import zio.schema.Schema
import zio.schema.codec.JsonCodec._
sealed trait ElasticRequest[+A] { self =>
  final def map[B](f: A => B): ElasticRequest[B] = ElasticRequest.Map(self, f)

}

object ElasticRequest {

  sealed trait Constructor[+A] extends ElasticRequest[A]

  private[elasticsearch] final case class Map[A, B](request: ElasticRequest[A], mapper: A => B)
      extends ElasticRequest[B]

  def getById[A: Schema](index: String, id: DocumentId, routing: Option[Routing] = None): ElasticRequest[Option[A]] =
    GetById(Index(index), id, routing).map {
      case Some(document) => JsonDecoder.decode(Schema[A], document.toJson).toOption
      case None           => None
    }

  private[elasticsearch] final case class GetById(
    index: Index,
    id: DocumentId,
    routing: Option[Routing] = None
  ) extends Constructor[Option[Document]]

}
