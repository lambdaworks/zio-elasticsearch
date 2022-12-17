package example

import example.api.{Application, Repositories}
import example.config.{AppConfig, HttpConfig}
import sttp.client3.httpclient.zio.HttpClientZioBackend
import zio._
import zio.config.getConfig
import zio.elasticsearch.ElasticExecutor
import zio.http.Server

object Main extends ZIOAppDefault {

//  def migrate(): RIO[ElasticExecutor, Unit] =
//    for {
//      conf    <- getConfig[ElasticsearchConfig]
//      _       <- Console.printLine(s"Elasticsearch on ${conf.host}:${conf.port}...")
//      mapping <- ZIO.attempt(Source.fromURL(getClass.getResource("mapping.json")).mkString)
//      _       <- ElasticRequest.createIndex(Index, Some(mapping)).execute
//    } yield ()
//
//  def a(): RIO[ElasticExecutor, Unit] =
//    for {
//      conf <- getConfig[ElasticsearchConfig]
//      _    <- Console.printLine(s"Elasticsearch on ${conf.host}:${conf.port}...")
//      _    <- ElasticRequest.deleteIndex(Index).execute
//    } yield ()

  override def run: Task[Unit] =
    (for {
      http  <- getConfig[HttpConfig]
      _     <- ZIO.logInfo(s"Starting an HTTP service on port: ${http.port}")
      routes = Application.Routes ++ Repositories.Routes
      _     <- Server.serve(routes)
    } yield ())
//      .onInterrupt(Console.printLine("Exiting...").orDie)
//      .onExit(_ => Console.printLine("Exiting..."))
      .provide(
        AppConfig.live,
        ElasticExecutor.local,
        RepositoriesElasticsearch.live,
        Server.default,
        HttpClientZioBackend.layer()
      )
}
