package example

import zio.elasticsearch.ElasticError.DocumentRetrievingError.DocumentNotFound
import zio.elasticsearch.{DocumentId, ElasticExecutor, ElasticRequest, Routing}
import zio.prelude.Newtype.unsafeWrap
import zio.{RIO, ULayer, ZIO, ZLayer}

final class RepositoriesElasticsearch {

  def findById(organization: String, id: String): RIO[ElasticExecutor, Option[Repository]] =
    ElasticRequest
      .getById[Repository](Index, DocumentId(id))
      .routing(unsafeWrap(Routing)(organization))
      .execute
      .map(_.toOption)

  def create(repository: Repository): RIO[ElasticExecutor, Option[DocumentId]] =
    ElasticRequest
      .create(Index, repository)
      .routing(unsafeWrap(Routing)(repository.organization))
      .execute

  def upsert(id: String, repository: Repository): RIO[ElasticExecutor, Unit] =
    ElasticRequest
      .upsert(Index, DocumentId(id), repository)
      .routing(unsafeWrap(Routing)(repository.organization))
      .execute
      .unit

  def remove(organization: String, id: String): RIO[ElasticExecutor, Either[DocumentNotFound.type, Unit]] =
    ElasticRequest
      .deleteById(Index, DocumentId(id))
      .routing(unsafeWrap(Routing)(organization))
      .execute

}

object RepositoriesElasticsearch {

  def findById(
    organization: String,
    id: String
  ): RIO[ElasticExecutor with RepositoriesElasticsearch, Option[Repository]] =
    ZIO.serviceWithZIO[RepositoriesElasticsearch](_.findById(organization, id))

  def create(repository: Repository): RIO[ElasticExecutor with RepositoriesElasticsearch, Option[DocumentId]] =
    ZIO.serviceWithZIO[RepositoriesElasticsearch](_.create(repository))

  def upsert(id: String, repository: Repository): RIO[ElasticExecutor with RepositoriesElasticsearch, Unit] =
    ZIO.serviceWithZIO[RepositoriesElasticsearch](_.upsert(id, repository))

  def remove(
    organization: String,
    id: String
  ): RIO[ElasticExecutor with RepositoriesElasticsearch, Either[DocumentNotFound.type, Unit]] =
    ZIO.serviceWithZIO[RepositoriesElasticsearch](_.remove(organization, id))

  lazy val live: ULayer[RepositoriesElasticsearch] = ZLayer.succeed(new RepositoriesElasticsearch())
}
