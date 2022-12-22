package example

import zio.elasticsearch.ElasticError.DocumentRetrievingError.DocumentNotFound
import zio.elasticsearch.{DocumentId, ElasticExecutor, ElasticRequest, Routing}
import zio.{RIO, Task, URLayer, ZIO, ZLayer}

final case class RepositoriesElasticsearch(executor: ElasticExecutor) {

  def findById(organization: String, id: String): Task[Option[GitHubRepo]] =
    for {
      routing <- Routing.make(organization).toZIO.mapError(e => new IllegalArgumentException(e))
      req      = ElasticRequest.getById[GitHubRepo](Index, DocumentId(id)).routing(routing)
      res     <- executor.execute(req)
    } yield res.toOption

  def create(repository: GitHubRepo): Task[Option[DocumentId]] =
    for {
      routing <- Routing.make(repository.organization).toZIO.mapError(e => new IllegalArgumentException(e))
      req      = ElasticRequest.create(Index, repository).routing(routing)
      res     <- executor.execute(req)
    } yield res

  def upsert(id: String, repository: GitHubRepo): Task[Unit] =
    for {
      routing <- Routing.make(repository.organization).toZIO.mapError(e => new IllegalArgumentException(e))
      req      = ElasticRequest.upsert(Index, DocumentId(id), repository).routing(routing)
      _       <- executor.execute(req)
    } yield ()

  def remove(organization: String, id: String): Task[Either[DocumentNotFound.type, Unit]] =
    for {
      routing <- Routing.make(organization).toZIO.mapError(e => new IllegalArgumentException(e))
      req      = ElasticRequest.deleteById(Index, DocumentId(id)).routing(routing)
      res     <- executor.execute(req)
    } yield res

}

object RepositoriesElasticsearch {

  def findById(organization: String, id: String): RIO[RepositoriesElasticsearch, Option[GitHubRepo]] =
    ZIO.serviceWithZIO[RepositoriesElasticsearch](_.findById(organization, id))

  def create(repository: GitHubRepo): RIO[RepositoriesElasticsearch, Option[DocumentId]] =
    ZIO.serviceWithZIO[RepositoriesElasticsearch](_.create(repository))

  def upsert(id: String, repository: GitHubRepo): RIO[RepositoriesElasticsearch, Unit] =
    ZIO.serviceWithZIO[RepositoriesElasticsearch](_.upsert(id, repository))

  def remove(organization: String, id: String): RIO[RepositoriesElasticsearch, Either[DocumentNotFound.type, Unit]] =
    ZIO.serviceWithZIO[RepositoriesElasticsearch](_.remove(organization, id))

  lazy val live: URLayer[ElasticExecutor, RepositoriesElasticsearch] =
    ZLayer.fromFunction(RepositoriesElasticsearch(_))
}
