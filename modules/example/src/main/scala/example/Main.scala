package example

import example.api.{HealthCheck, Repositories}
import example.config.{AppConfig, ElasticsearchConfig, HttpConfig}
import example.external.github.RepoFetcher
import sttp.client3.httpclient.zio.HttpClientZioBackend
import zio.ZIO.collectAll
import zio._
import zio.config.getConfig
import zio.elasticsearch.{ElasticConfig, ElasticExecutor, ElasticRequest}
import zio.http.{Server, ServerConfig}

import scala.io.Source

object Main extends ZIOAppDefault {

  override def run: Task[ExitCode] = {
    val elasticConfigLive = ZLayer(getConfig[ElasticsearchConfig].map(es => ElasticConfig(es.host, es.port)))

    (prepare *> populate *> runServer).provide(
      AppConfig.live,
      elasticConfigLive,
      ElasticExecutor.live,
      HttpClientZioBackend.layer()
    )
  }

  private[this] def populate: RIO[ElasticExecutor, Unit] = {
    val repositories = RepoFetcher.fetchAllByOrganization("zio")

    collectAll(
      repositories.map(repository =>
        for {
          _ <- ZIO.logInfo(s"Adding GitHub repository '${repository.name}'...")
          _ <- RepositoriesElasticsearch.create(repository)
        } yield ()
      )
    ).unit.provideSome(RepositoriesElasticsearch.live)
  }

  private[this] def prepare: RIO[ElasticExecutor, Unit] = {
    val deleteIndex: RIO[ElasticExecutor, Unit] =
      for {
        _ <- ZIO.logInfo(s"Deleting index '$Index'...")
        _ <- ElasticRequest.deleteIndex(Index).execute
      } yield ()

    val createIndex: RIO[ElasticExecutor, Unit] =
      for {
        _       <- ZIO.logInfo(s"Creating index '$Index'...")
        mapping <- ZIO.attempt(Source.fromURL(getClass.getResource("/mapping.json")).mkString)
        _       <- ElasticRequest.createIndex(Index, Some(mapping)).execute
      } yield ()

    deleteIndex *> createIndex
  }

  private[this] def runServer: RIO[HttpConfig with ElasticExecutor, ExitCode] = {
    val serverConfigLive = ZLayer(getConfig[HttpConfig].map(http => ServerConfig.default.port(http.port)))

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
