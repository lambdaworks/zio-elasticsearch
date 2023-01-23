package example

import zio._
import zio.elasticsearch.{DeletionOutcome, DocumentId, ElasticExecutor, ElasticRequest, Routing}
import zio.prelude.Newtype.unsafeWrap

final case class RepositoriesElasticsearch(executor: ElasticExecutor) {

  def findById(organization: String, id: String): Task[Option[GitHubRepo]] =
    for {
      routing <- routingOf(organization)
      req      = ElasticRequest.getById[GitHubRepo](Index, DocumentId(id)).routing(routing)
      res     <- executor.execute(req)
    } yield res

  def create(repository: GitHubRepo): Task[DocumentId] =
    for {
      routing <- routingOf(repository.organization)
      req      = ElasticRequest.create(Index, repository).routing(routing).refreshTrue
      res     <- executor.execute(req)
    } yield res

  def createAll(repositories: List[GitHubRepo]): Task[Unit] =
    for {
      reqs <- ZIO.collectAllPar {
                repositories.map { repository =>
                  routingOf(repository.organization).map(
                    ElasticRequest
                      .create[GitHubRepo](Index, unsafeWrap(DocumentId)(repository.id), repository)
                      .routing(_)
                  )
                }
              }
      bulkReq = ElasticRequest.bulk(reqs: _*)
      _      <- executor.execute(bulkReq)
    } yield ()

  def upsert(id: String, repository: GitHubRepo): Task[Unit] =
    for {
      routing <- routingOf(repository.organization)
      req      = ElasticRequest.upsert(Index, DocumentId(id), repository).routing(routing).refresh(value = true)
      _       <- executor.execute(req)
    } yield ()

  def remove(organization: String, id: String): Task[DeletionOutcome] =
    for {
      routing <- routingOf(organization)
      req      = ElasticRequest.deleteById(Index, DocumentId(id)).routing(routing).refreshFalse
      res     <- executor.execute(req)
    } yield res

  private def routingOf(value: String): IO[IllegalArgumentException, Routing.Type] =
    Routing.make(value).toZIO.mapError(e => new IllegalArgumentException(e))

}

object RepositoriesElasticsearch {

  def findById(organization: String, id: String): RIO[RepositoriesElasticsearch, Option[GitHubRepo]] =
    ZIO.serviceWithZIO[RepositoriesElasticsearch](_.findById(organization, id))

  def create(repository: GitHubRepo): RIO[RepositoriesElasticsearch, DocumentId] =
    ZIO.serviceWithZIO[RepositoriesElasticsearch](_.create(repository))

  def createAll(repositories: List[GitHubRepo]): RIO[RepositoriesElasticsearch, Unit] =
    ZIO.serviceWithZIO[RepositoriesElasticsearch](_.createAll(repositories))

  def upsert(id: String, repository: GitHubRepo): RIO[RepositoriesElasticsearch, Unit] =
    ZIO.serviceWithZIO[RepositoriesElasticsearch](_.upsert(id, repository))

  def remove(organization: String, id: String): RIO[RepositoriesElasticsearch, DeletionOutcome] =
    ZIO.serviceWithZIO[RepositoriesElasticsearch](_.remove(organization, id))

  lazy val live: URLayer[ElasticExecutor, RepositoriesElasticsearch] =
    ZLayer.fromFunction(RepositoriesElasticsearch(_))
}
