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
import sttp.client3.SttpBackend
import sttp.client3.httpclient.zio.HttpClientZioBackend
import zio._
import zio.config.getConfig
import zio.elasticsearch.{ElasticConfig, ElasticExecutor, ElasticRequest}
import zio.http.{Server, ServerConfig}

import scala.io.Source
import scala.util.Using

object Main extends ZIOAppDefault {

  override def run: Task[ExitCode] = {
    val elasticConfigLive = ZLayer(getConfig[ElasticsearchConfig].map(es => ElasticConfig(es.host, es.port)))

    (prepare *> runServer).provide(
      AppConfig.live,
      elasticConfigLive,
      ElasticExecutor.live,
      HttpClientZioBackend.layer()
    )
  }

  private[this] def prepare: RIO[SttpBackend[Task, Any] with ElasticExecutor, Unit] = {
    val deleteIndex: RIO[ElasticExecutor, Unit] =
      for {
        _ <- ZIO.logInfo(s"Deleting index '$Index'...")
        _ <- ElasticRequest.deleteIndex(Index).execute
      } yield ()

    val createIndex: RIO[ElasticExecutor, Unit] =
      for {
        _       <- ZIO.logInfo(s"Creating index '$Index'...")
        mapping <- ZIO.fromTry(Using(Source.fromURL(getClass.getResource("/mapping.json")))(_.mkString))
        _       <- ElasticRequest.createIndex(Index, Some(mapping)).execute
      } yield ()

    val populate: RIO[SttpBackend[Task, Any] with ElasticExecutor, Unit] =
      (for {
        repositories <- RepoFetcher.fetchAllByOrganization(organization)
        _            <- ZIO.logInfo("Adding GitHub repositories...")
        _            <- RepositoriesElasticsearch.createAll(repositories)
      } yield ()).provideSome(RepositoriesElasticsearch.live)

    deleteIndex *> createIndex *> populate
  }

  private[this] def runServer: RIO[HttpConfig with ElasticExecutor, ExitCode] = {
    val serverConfigLive = ZLayer.fromFunction((http: HttpConfig) => ServerConfig.default.port(http.port))

    (for {
      http  <- getConfig[HttpConfig]
      _     <- ZIO.logInfo(s"Starting an HTTP service on port: ${http.port}")
      routes = HealthCheck.Route ++ Repositories.Routes
      _     <- Server.serve(routes)
    } yield ExitCode.success).provideSome(
      RepositoriesElasticsearch.live,
      Server.live,
      serverConfigLive
    )
  }

}
