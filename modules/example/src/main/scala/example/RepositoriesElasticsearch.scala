/*
 * Copyright 2022 LambdaWorks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package example

import example.RepositoryError.{ElasticsearchError, InvalidRouting}
import zio._
import zio.elasticsearch.ElasticQuery.matchAll
import zio.elasticsearch._
import zio.elasticsearch.query.ElasticQuery
import zio.elasticsearch.request.{CreationOutcome, DeletionOutcome}

final case class RepositoriesElasticsearch(elasticsearch: Elasticsearch) {

  def findAll(): Task[Chunk[GitHubRepo]] =
    elasticsearch.execute(ElasticRequest.search(Index, matchAll)).documentAs[GitHubRepo]

  def findById(organization: String, id: String): IO[RepositoryError, Option[GitHubRepo]] =
    for {
      routing <- routingOf(organization)
      res     <- elasticsearch
               .execute(ElasticRequest.getById(Index, DocumentId(id)).routing(routing))
               .documentAs[GitHubRepo]
               .mapError(ElasticsearchError(_))
    } yield res

  def create(repository: GitHubRepo): IO[RepositoryError, CreationOutcome] =
    for {
      routing <- routingOf(repository.organization)
      res     <- elasticsearch
               .execute(
                 ElasticRequest.create(Index, DocumentId(repository.id), repository).routing(routing).refreshTrue
               )
               .mapError(ElasticsearchError(_))
    } yield res

  def createAll(repositories: Chunk[GitHubRepo]): IO[RepositoryError, Unit] =
    for {
      routing <- routingOf(organization)
      _       <- elasticsearch
             .execute(
               ElasticRequest
                 .bulk(repositories.map { repository =>
                   ElasticRequest.create[GitHubRepo](Index, DocumentId(repository.id), repository)
                 }: _*)
                 .routing(routing)
             )
             .mapError(ElasticsearchError(_))
    } yield ()

  def upsertBulk(organization: String, repositories: Chunk[GitHubRepo]): IO[RepositoryError, Unit] =
    for {
      routing     <- routingOf(organization)
      bulkRequests = repositories.map(repo => ElasticRequest.upsert(DocumentId(repo.id), repo).routing(routing))
      _           <- elasticsearch.execute(ElasticRequest.bulk(Index, bulkRequests: _*)).mapError(ElasticsearchError(_))
    } yield ()

  def upsert(id: String, repository: GitHubRepo): IO[RepositoryError, Unit] =
    for {
      routing <- routingOf(repository.organization)
      _       <- elasticsearch
             .execute(
               ElasticRequest.upsert(Index, DocumentId(id), repository).routing(routing).refresh(value = true)
             )
             .mapError(ElasticsearchError(_))
    } yield ()

  def remove(organization: String, id: String): IO[RepositoryError, DeletionOutcome] =
    for {
      routing <- routingOf(organization)
      res     <- elasticsearch
               .execute(ElasticRequest.deleteById(Index, DocumentId(id)).routing(routing).refreshFalse)
               .mapError(ElasticsearchError(_))
    } yield res

  def search(query: ElasticQuery[_], from: Int, size: Int): Task[Chunk[GitHubRepo]] =
    elasticsearch.execute(ElasticRequest.search(Index, query).from(from).size(size)).documentAs[GitHubRepo]

  private def routingOf(value: String): IO[RepositoryError, Routing.Type] =
    Routing.make(value).toZIO.mapError(e => InvalidRouting(e))
}

object RepositoriesElasticsearch {

  def findAll(): RIO[RepositoriesElasticsearch, Chunk[GitHubRepo]] =
    ZIO.serviceWithZIO[RepositoriesElasticsearch](_.findAll())

  def findById(organization: String, id: String): ZIO[RepositoriesElasticsearch, RepositoryError, Option[GitHubRepo]] =
    ZIO.serviceWithZIO[RepositoriesElasticsearch](_.findById(organization, id))

  def create(repository: GitHubRepo): ZIO[RepositoriesElasticsearch, RepositoryError, CreationOutcome] =
    ZIO.serviceWithZIO[RepositoriesElasticsearch](_.create(repository))

  def createAll(repositories: Chunk[GitHubRepo]): ZIO[RepositoriesElasticsearch, RepositoryError, Unit] =
    ZIO.serviceWithZIO[RepositoriesElasticsearch](_.createAll(repositories))

  def upsertBulk(
    organization: String,
    repositories: Chunk[GitHubRepo]
  ): ZIO[RepositoriesElasticsearch, RepositoryError, Unit] =
    ZIO.serviceWithZIO[RepositoriesElasticsearch](_.upsertBulk(organization, repositories))

  def upsert(id: String, repository: GitHubRepo): ZIO[RepositoriesElasticsearch, RepositoryError, Unit] =
    ZIO.serviceWithZIO[RepositoriesElasticsearch](_.upsert(id, repository))

  def remove(organization: String, id: String): ZIO[RepositoriesElasticsearch, RepositoryError, DeletionOutcome] =
    ZIO.serviceWithZIO[RepositoriesElasticsearch](_.remove(organization, id))

  def search(query: ElasticQuery[_], from: Int, size: Int): RIO[RepositoriesElasticsearch, Chunk[GitHubRepo]] =
    ZIO.serviceWithZIO[RepositoriesElasticsearch](_.search(query, from, size))

  lazy val live: URLayer[Elasticsearch, RepositoriesElasticsearch] =
    ZLayer.fromFunction(RepositoriesElasticsearch(_))
}
