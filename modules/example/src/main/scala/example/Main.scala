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

import example.api.{HealthCheck, Repositories}
import example.config.{AppConfig, ElasticsearchConfig, HttpConfig}
import example.external.github.RepoFetcher
import sttp.client4.httpclient.zio.{HttpClientZioBackend, SttpClient}
import zio._
import zio.config.typesafe.TypesafeConfigProvider
import zio.elasticsearch.{ElasticConfig, ElasticExecutor, ElasticRequest, Elasticsearch}
import zio.http.Server

import scala.io.Source
import scala.util.Using

object Main extends ZIOAppDefault {

  override val bootstrap: ULayer[Unit] =
    Runtime.setConfigProvider(TypesafeConfigProvider.fromResourcePath())

  def run: Task[ExitCode] = {
    val elasticConfigLive = ZLayer(ZIO.serviceWith[ElasticsearchConfig](es => ElasticConfig(es.host, es.port)))

    (prepare *> runServer).provide(
      AppConfig.live,
      elasticConfigLive,
      ElasticExecutor.live,
      Elasticsearch.layer,
      HttpClientZioBackend.layer()
    )
  }

  private[this] def prepare: RIO[SttpClient with Elasticsearch, Unit] = {
    val deleteIndex: RIO[Elasticsearch, Unit] =
      for {
        _ <- ZIO.logInfo(s"Deleting index '$Index'...")
        _ <- Elasticsearch.execute(ElasticRequest.deleteIndex(Index))
      } yield ()

    val createIndex: RIO[Elasticsearch, Unit] =
      for {
        _       <- ZIO.logInfo(s"Creating index '$Index'...")
        mapping <- ZIO.fromTry(Using(Source.fromURL(getClass.getResource("/mapping.json")))(_.mkString))
        _       <- Elasticsearch.execute(ElasticRequest.createIndex(Index, mapping))
      } yield ()

    val populate: RIO[SttpClient with Elasticsearch, Unit] =
      (for {
        repositories <- RepoFetcher.fetchAllByOrganization(organization)
        _            <- ZIO.logInfo("Adding GitHub repositories...")
        _            <- RepositoriesElasticsearch.createAll(repositories)
      } yield ()).provideSome(RepositoriesElasticsearch.live)

    deleteIndex *> createIndex *> populate
  }

  private[this] def runServer: RIO[HttpConfig with Elasticsearch, ExitCode] = {
    val serverConfigLive = ZLayer.fromFunction((http: HttpConfig) => Server.Config.default.port(http.port))

    (for {
      http  <- ZIO.service[HttpConfig]
      _     <- ZIO.logInfo(s"Starting an HTTP service on port: ${http.port}")
      routes = HealthCheck.health ++ Repositories.routes
      _     <- Server.serve(routes)
    } yield ExitCode.success).provideSome(
      RepositoriesElasticsearch.live,
      Server.live,
      serverConfigLive
    )
  }

}
