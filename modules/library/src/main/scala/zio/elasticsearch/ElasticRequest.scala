package zio.elasticsearch

import zio.schema.Schema

sealed trait ElasticRequest[+A] { self =>
  final def map[B](f: A => B): ElasticRequest[B] = ElasticRequest.Map(self, f)

}

object ElasticRequest {

  private[elasticsearch] final case class Map[A, B](request: ElasticRequest[A], mapper: A => B)
      extends ElasticRequest[B]

  def getById[A: Schema](index: Index, id: DocumentId, routing: Option[Routing] = None): ElasticRequest[Option[A]] =
    GetById(index, id, routing).map {
      case Some(document) => document.decode.toOption
      case None           => None
    }

  private[elasticsearch] final case class GetById(
    index: Index,
    id: DocumentId,
    routing: Option[Routing] = None
  ) extends ElasticRequest[Option[Document]]

}
