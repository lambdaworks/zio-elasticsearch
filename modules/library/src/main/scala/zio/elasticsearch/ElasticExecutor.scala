package zio.elasticsearch

import sttp.client3.SttpBackend
import sttp.client3.httpclient.zio.HttpClientZioBackend
import zio.{Task, ZIO, ZLayer}

trait ElasticExecutor {
  def execute[A](request: ElasticRequest[A]): Task[A]
}

object ElasticExecutor {
  def live(config: ElasticConfig): ZLayer[ElasticConfig, Throwable, ElasticExecutor] =
    ZLayer.fromZIO {
      (for {
        conf <- ZIO.service[ElasticConfig]
        sttp <- ZIO.service[SttpBackend[Task, Any]]
      } yield HttpElasticExecutor.create(conf, sttp))
        .provide(HttpClientZioBackend.layer(), ZLayer.succeed(config))
    }

  lazy val local: ZLayer[Any, Throwable, ElasticExecutor] =
    ZLayer.fromZIO {
      (for {
        conf <- ZIO.service[ElasticConfig]
        sttp <- ZIO.service[SttpBackend[Task, Any]]
      } yield HttpElasticExecutor.create(conf, sttp))
        .provide(HttpClientZioBackend.layer(), ZLayer.succeed(ElasticConfig.Default))
    }
}
