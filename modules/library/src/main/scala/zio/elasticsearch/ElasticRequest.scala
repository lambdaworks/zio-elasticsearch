package zio.elasticsearch

import zio.schema.Schema

sealed trait ElasticRequest[+A]

object ElasticRequest {

  sealed trait Constructor[+A] extends ElasticRequest[A]

  def getById[A: Schema](index: String, id: DocumentId, routing: Option[Routing] = None): ElasticRequest[Option[A]] =
    GetById(Index(index), id, routing)

  private[elasticsearch] final case class GetById[A: Schema](
    index: Index,
    id: DocumentId,
    routing: Option[Routing] = None
  ) extends Constructor[Option[A]]

}
