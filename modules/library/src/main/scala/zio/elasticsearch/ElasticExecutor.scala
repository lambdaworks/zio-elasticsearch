package zio.elasticsearch

import sttp.client3.SttpBackend
import zio.stm.TMap
import zio.{Task, ULayer, ZIO, ZLayer}

trait ElasticExecutor {
  def execute[A](request: ElasticRequest[A, _]): Task[A]
}

object ElasticExecutor {
  lazy val live: ZLayer[ElasticConfig with SttpBackend[Task, Any], Throwable, ElasticExecutor] =
    ZLayer {
      for {
        conf <- ZIO.service[ElasticConfig]
        sttp <- ZIO.service[SttpBackend[Task, Any]]
      } yield HttpElasticExecutor(conf, sttp)
    }

  lazy val local: ZLayer[SttpBackend[Task, Any], Throwable, ElasticExecutor] =
    ZLayer.succeed(ElasticConfig.Default) >>> live

  lazy val test: ULayer[TestExecutor] =
    ZLayer(
      TMap.empty[IndexName, TMap[DocumentId, Document]].map(TestExecutor).commit
    )
}
