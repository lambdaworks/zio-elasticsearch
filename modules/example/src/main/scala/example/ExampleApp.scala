package example

import sttp.client3.httpclient.zio.HttpClientZioBackend
import zio._
import zio.elasticsearch._

object ExampleApp extends ZIOAppDefault {

  override def run: Task[Unit] = {
    val index   = IndexName("examples")
    val docId   = DocumentId("test-document-1")
    val routing = Some(Routing("10"))

    (for {
      _   <- Console.printLine("Welcome to an example app...")
      _   <- Console.printLine(s"Looking for the document '$docId' in '$index' index...'")
      res <- ElasticRequest.getById[ExampleDocument](index, docId, routing).execute
      _   <- Console.printLine(res)
    } yield ()).provide(ElasticExecutor.local, HttpClientZioBackend.layer())
  }
}
