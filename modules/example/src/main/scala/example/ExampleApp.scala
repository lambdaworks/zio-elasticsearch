package example

import zio._
import zio.elasticsearch.{DocumentId, ElasticExecutor, ElasticRequest, IndexName}

object ExampleApp extends ZIOAppDefault {

  override def run: Task[Unit] =
    (for {
      _  <- Console.printLine("Welcome to an example app...")
      es <- ZIO.service[ElasticExecutor]
      req =
        ElasticRequest
          .getById[ExampleDocument](IndexName("kibana_sample_data_ecommerce"), DocumentId("NjvbqIQB22WMP-4s5SrH"))
      /*req2 = ElasticRequest.put[ExampleDocument](
               IndexName("kibana_sample_data_ecommerce"),
               DocumentId("NjvbqIQB22WMP-4s5SrH"),
               ExampleDocument("String", 10)
             )*/
      res <- es.execute(req)
//      res2 <- es.execute(req2)
      _ <- Console.printLine(res)
//      _ <- Console.printLine(res2)
    } yield ()).provide(ElasticExecutor.local)
}
