package zio.elasticsearch

import sttp.client3.httpclient.zio.HttpClientZioBackend
import zio.{Task, ZLayer}

trait ElasticExecutor {
  def execute[A](request: ElasticRequest[A]): Task[Document]
}

object ElasticExecutor {
  lazy val live: ZLayer[Any, Throwable, ElasticExecutor] =
    HttpClientZioBackend.layer() >>> ZLayer.fromFunction(HttpElasticExecutor.create(_))
}
