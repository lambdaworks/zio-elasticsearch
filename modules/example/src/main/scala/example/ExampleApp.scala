package example

import zio._
import zio.elasticsearch.{DocumentId, ElasticExecutor, ElasticRequest, IndexName, Routing}

object ExampleApp extends ZIOAppDefault {

  override def run: Task[Unit] = {
    val index   = IndexName("examples")
    val docId   = DocumentId("test-document-2")
    val routing = Some(Routing("10"))

    (for {
      _   <- Console.printLine("Welcome to an example app...")
      _   <- Console.printLine(s"Looking for the document '$docId' in '$index' index...'")
      res <- ElasticRequest.getById[ExampleDocument](index, docId, routing).execute
      _   <- Console.printLine(res)
    } yield ()).provide(ElasticExecutor.local)
  }
}
