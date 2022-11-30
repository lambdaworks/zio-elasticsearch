package example

import sttp.client3.httpclient.zio.HttpClientZioBackend
import zio.{Task, ULayer, ZIO, ZLayer}
import zio.elasticsearch.{DocumentId, ElasticExecutor, ElasticRequest, IndexName, Routing}

final class RepositoriesElasticsearch {

  private val index = IndexName("repositories")

  def one(id: String): Task[Option[Repository]] =
    ElasticRequest
      .getById[Repository](index, DocumentId(id))
      .execute
      .map(_.toOption)
      .provide(ElasticExecutor.live, HttpClientZioBackend.layer())

  def create(repository: Repository): Task[Option[DocumentId]] =
    ElasticRequest
      .create(index, repository, Some(Routing("test")))
      .execute
      .provide(ElasticExecutor.live, HttpClientZioBackend.layer())

  def upsert(id: String, repository: Repository): Task[Unit] =
    ElasticRequest
      .upsert(index, DocumentId(id), repository)
      .execute
      .unit
      .provide(ElasticExecutor.live, HttpClientZioBackend.layer())

}

object RepositoriesElasticsearch {
  lazy val live: ULayer[RepositoriesElasticsearch] = ZLayer.succeed(new RepositoriesElasticsearch())
}
