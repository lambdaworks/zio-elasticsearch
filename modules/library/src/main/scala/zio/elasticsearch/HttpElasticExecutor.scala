package zio.elasticsearch

import sttp.client3._
import zio.elasticsearch.ElasticRequest.{GetById, Map}
import zio.{Task, ZIO}

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

private[elasticsearch] object HttpElasticExecutor {
  def create(client: SttpBackend[Task, Any]) =
    new HttpElasticExecutor(client)
}
