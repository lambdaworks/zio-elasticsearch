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

import zio._
import zio.elasticsearch.ElasticQuery.matchAll
import zio.elasticsearch.{
  CreationOutcome,
  DeletionOutcome,
  DocumentId,
  ElasticQuery,
  ElasticRequest,
  Elasticsearch,
  Routing
}
import zio.prelude.Newtype.unsafeWrap

final case class RepositoriesElasticsearch(elasticsearch: Elasticsearch) {

  def findAll(): Task[List[GitHubRepo]] =
    for {
      aa <- elasticsearch.execute(ElasticRequest.count(Index, matchAll))
      _   = println(aa)
    } yield List()

  def findById(organization: String, id: String): Task[Option[GitHubRepo]] =
    for {
      routing <- routingOf(organization)
      res <- elasticsearch
               .execute(ElasticRequest.getById(Index, DocumentId(id)).routing(routing))
               .documentAs[GitHubRepo]
    } yield res

  def create(repository: GitHubRepo): Task[CreationOutcome] =
    for {
      routing <- routingOf(repository.organization)
      res <- elasticsearch.execute(
               ElasticRequest.create(Index, DocumentId(repository.id), repository).routing(routing).refreshTrue
             )
    } yield res

  def createAll(repositories: List[GitHubRepo]): Task[Unit] =
    for {
      routing <- routingOf(organization)
      _ <- elasticsearch.execute(
             ElasticRequest
               .bulk(repositories.map { repository =>
                 ElasticRequest.create[GitHubRepo](Index, unsafeWrap(DocumentId)(repository.id), repository)
               }: _*)
               .routing(routing)
           )
    } yield ()

  def upsert(id: String, repository: GitHubRepo): Task[Unit] =
    for {
      routing <- routingOf(repository.organization)
      _ <- elasticsearch.execute(
             ElasticRequest.upsert(Index, DocumentId(id), repository).routing(routing).refresh(value = true)
           )
    } yield ()

  def remove(organization: String, id: String): Task[DeletionOutcome] =
    for {
      routing <- routingOf(organization)
      res     <- elasticsearch.execute(ElasticRequest.deleteById(Index, DocumentId(id)).routing(routing).refreshFalse)
    } yield res

  def search(query: ElasticQuery[_]): Task[List[GitHubRepo]] =
    elasticsearch.execute(ElasticRequest.search(Index, query)).documentAs[GitHubRepo]

  private def routingOf(value: String): IO[IllegalArgumentException, Routing.Type] =
    Routing.make(value).toZIO.mapError(e => new IllegalArgumentException(e))

}

object RepositoriesElasticsearch {

  def findAll(): RIO[RepositoriesElasticsearch, List[GitHubRepo]] =
    ZIO.serviceWithZIO[RepositoriesElasticsearch](_.findAll())

  def findById(organization: String, id: String): RIO[RepositoriesElasticsearch, Option[GitHubRepo]] =
    ZIO.serviceWithZIO[RepositoriesElasticsearch](_.findById(organization, id))

  def create(repository: GitHubRepo): RIO[RepositoriesElasticsearch, CreationOutcome] =
    ZIO.serviceWithZIO[RepositoriesElasticsearch](_.create(repository))

  def createAll(repositories: List[GitHubRepo]): RIO[RepositoriesElasticsearch, Unit] =
    ZIO.serviceWithZIO[RepositoriesElasticsearch](_.createAll(repositories))

  def upsert(id: String, repository: GitHubRepo): RIO[RepositoriesElasticsearch, Unit] =
    ZIO.serviceWithZIO[RepositoriesElasticsearch](_.upsert(id, repository))

  def remove(organization: String, id: String): RIO[RepositoriesElasticsearch, DeletionOutcome] =
    ZIO.serviceWithZIO[RepositoriesElasticsearch](_.remove(organization, id))

  def search(query: ElasticQuery[_]): RIO[RepositoriesElasticsearch, List[GitHubRepo]] =
    ZIO.serviceWithZIO[RepositoriesElasticsearch](_.search(query))

  lazy val live: URLayer[Elasticsearch, RepositoriesElasticsearch] =
    ZLayer.fromFunction(RepositoriesElasticsearch(_))
}
