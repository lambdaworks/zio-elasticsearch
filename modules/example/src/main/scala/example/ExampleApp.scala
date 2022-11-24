package example

import zio._
import zio.elasticsearch.{DocumentId, ElasticExecutor, ElasticRequest, IndexName}
import zio.schema._

object ExampleApp extends ZIOAppDefault {

  final case class ExampleDocument(id: String, count: Int)

  implicit val exampleDocumentSchema: Schema[ExampleDocument] = DeriveSchema.gen[ExampleDocument]

  override def run: Task[Unit] =
    (for {
      _  <- Console.printLine("Welcome to an example app...")
      es <- ZIO.service[ElasticExecutor]
      //      req  = ElasticRequest.getById[ExampleDocument](IndexName("test"), DocumentId("id")).execute
      req =
        ElasticRequest
          .getById[ExampleDocument](IndexName("kibana_sample_data_ecommerce"), DocumentId("NjvbqIQB22WMP-4s5SrH"))
      res <- es.execute(req)
      _   <- Console.printLine(res)
    } yield ()).provide(ElasticExecutor.live)
}
