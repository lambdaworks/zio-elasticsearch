package zio.elasticsearch

import sttp.client3._
import sttp.client3.httpclient.zio.HttpClientZioBackend
import zio.elasticsearch.ElasticRequest.{GetById, Map}
import zio.{Task, ZIO, ZLayer}

import scala.annotation.tailrec

private[elasticsearch] final class HttpElasticExecutor private (client: SttpBackend[Task, Any])
    extends ElasticExecutor {

  // TODO: ElasticConfig(url, port)
  // FIXME: execute: Task[A]
  // TODO: Use URL instead of String
  // TODO: Change zio-http with sttp
  // TODO: Provide override def toString to `IndexName` and `DocumentId`
  // TODO: Add .execute on Request (check DynamoDB)
  // TODO: Provide execute for put

  @tailrec
  override def execute[A](request: ElasticRequest[A]): Task[Document] =
    request match {
      case r: GetById      => executeGetById(r)
      case map @ Map(_, _) => execute(map.request) // .map(d => map.mapper)
      case _               => ZIO.fail(new RuntimeException("Not implemented yet."))
    }

  private def executeGetById(getById: GetById) = {
    val request = basicRequest.get(
      uri"http://localhost:9200/${getById.index.name}/_doc/${getById.id.value}"
    )
    println(request.send(client))
    ZIO.succeed(Document("""{"id": "lambdaworks", "count": 42}"""))
  }

}

object HttpElasticExecutor {
  def create(httpClient: HttpClientZioBackend): HttpElasticExecutor = new HttpElasticExecutor(httpClient)

  lazy val layer: ZLayer[HttpClientZioBackend, Throwable, ElasticExecutor] = ZLayer.scoped {
    for {
      httpClient <- ZIO.service[HttpClientZioBackend]
      executor    = create(httpClient)
      _          <- ZIO.succeed("Starting executor...")
    } yield executor
  }
}
