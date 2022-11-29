package example

import sttp.client3.httpclient.zio.HttpClientZioBackend
import zio._
import zio.elasticsearch._

object ExampleApp extends ZIOAppDefault {

  override def run: Task[Unit] = {
    val index = IndexName("documentindex")
    val docId = DocumentId("SrBtwIQBGQ08MH8or_0t")

    (for {
      _ <- Console.printLine("Welcome to an example app...")
      _ <- Console.printLine(s"Creating document with id '$docId' in '$index' index...'")
      newDocId <-
        ElasticRequest
          .create(index = index, id = docId, doc = ExampleDocument("docId", "docName", 11))
          .routing("10")
          .execute
      _ <- Console.printLine(newDocId)
      _ <- Console.printLine("Creating document with same id as previous (unsuccessfully)...")
      newDocId2 <-
        ElasticRequest
          .create(index = index, id = docId, doc = ExampleDocument("docId2", "docName2", 22))
          .routing("10")
          .execute
      _ <- Console.printLine(newDocId2)
      _ <- Console.printLine("Updating existing document...")
      _ <-
        ElasticRequest
          .upsert(index = index, id = docId, doc = ExampleDocument("docId3", "docName3", 33))
          .routing("10")
          .execute
      _   <- Console.printLine("Getting updated document...")
      res <- ElasticRequest.getById[ExampleDocument](index, docId).routing("10").execute
      _   <- Console.printLine(res)
    } yield ()).provide(ElasticExecutor.local, HttpClientZioBackend.layer())
  }
}
