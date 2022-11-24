package zio.elasticsearch

import sttp.client3.httpclient.zio.HttpClientZioBackend
import zio.{Task, ZLayer}

trait ElasticExecutor {
  def execute[A](request: ElasticRequest[A]): Task[Document]
}

object ElasticExecutor {
  lazy val layer: ZLayer[Any with HttpClientZioBackend, Throwable, ElasticExecutor] =
    HttpClientZioBackend.layer >>> HttpElasticExecutor.layer
}
