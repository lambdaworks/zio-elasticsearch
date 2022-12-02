package example

import zio.{RIO, ULayer, ZLayer}
import zio.elasticsearch.{DocumentId, ElasticExecutor, ElasticRequest, IndexName, Routing}

final class RepositoriesElasticsearch {

  private val index = IndexName("repositories")

  def one(id: String): RIO[ElasticExecutor, Option[Repository]] =
    ElasticRequest
      .getById[Repository](index, DocumentId(id))
      .execute
      .map(_.toOption)

  def create(repository: Repository): RIO[ElasticExecutor, Option[DocumentId]] =
    ElasticRequest
      .create(index, repository, Some(Routing("test")))
      .execute

  def upsert(id: String, repository: Repository): RIO[ElasticExecutor, Unit] =
    ElasticRequest
      .upsert(index, DocumentId(id), repository)
      .execute
      .unit

}

object RepositoriesElasticsearch {
  lazy val live: ULayer[RepositoriesElasticsearch] = ZLayer.succeed(new RepositoriesElasticsearch())
}
