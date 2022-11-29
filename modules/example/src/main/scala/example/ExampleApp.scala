package example

import sttp.client3.httpclient.zio.HttpClientZioBackend
import zio._
import zio.elasticsearch._

object ExampleApp extends ZIOAppDefault {
  override def run: Task[Unit] =
    // fixme: How about doing something like this as example app to avoid changes that get forgotten about?
    // fixme: We can even include match case for options like get,put,delete....
    (for {
      _             <- Console.printLine("Welcome to an example app...")
      index         <- Console.readLine("Index name: ")
      docId         <- Console.readLine("Document ID: ")
      routingString <- Console.readLine("Routing: ")
      routing        = if (routingString.trim.isEmpty) None else Some(Routing(routingString))
      _             <- Console.printLine(s"Looking for the document '$docId' in '$index' index...'")
      res           <- ElasticRequest.getById[ExampleDocument](IndexName(index), DocumentId(docId), routing).execute
      _             <- Console.printLine(res)
    } yield ()).provide(ElasticExecutor.local, HttpClientZioBackend.layer())
}
