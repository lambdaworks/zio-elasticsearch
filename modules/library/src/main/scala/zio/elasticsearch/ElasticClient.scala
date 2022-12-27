package zio.elasticsearch

import zio.{Task, ZLayer}

abstract class ElasticClient(executor: ElasticExecutor) {

  protected implicit class RunnableRequest[A](request: ElasticRequest[A]) {
    def run: Task[A] = request.execute.provideLayer(ZLayer.succeed(executor))
  }

}
