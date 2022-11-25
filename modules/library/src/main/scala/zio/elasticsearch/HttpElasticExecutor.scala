package zio.elasticsearch

import sttp.client3._
import zio.elasticsearch.ElasticRequest.{GetById, Map, Put}
import zio.json.DecoderOps
import zio.{Task, ZIO}

private[elasticsearch] final class HttpElasticExecutor private (config: ElasticConfig, client: SttpBackend[Task, Any])
    extends ElasticExecutor {

  private val baseUrl: String = s"http://${config.host}:${config.port}"

  // TODO: ElasticConfig(url, port)
  // FIXME: execute: Task[A]
  // TODO: Use URL instead of String
  // TODO: Change zio-http with sttp
  // TODO: Add .execute on Request (check DynamoDB)

  // TODO: Parse from elastic search JSON

  override def execute[A](request: ElasticRequest[A]): Task[A] =
    request match {
      case c: Constructor[_] => executeConstructor(c)
      case map @ Map(_, _)   => execute(map.request).map(map.mapper)
    }

  private def executeConstructor[A](constructor: Constructor[A]): Task[A] =
    constructor match {
      case getById: GetById => executeGetById(getById)
      case put: Put         => executePut(put)
    }

  /*
   * `executePut` can be done with and without Id.
   *
   * To execute it without id following has to be used:
   *
   * POST /baseURL/<index>/_doc/
   *
   * To execute it with id following has to be used:
   *
   * POST /baseURL/<index>/_doc/<id>
   * PUT /baseURL/<index>/_create/<id>
   * POST /baseURL/<index>/_create/<id>
   * */

  private def executePut(put: Put): Task[Unit] = {
    val request = basicRequest
      .post(
        uri"$baseUrl/${put.index}/_doc/"
      )
      .body(putBody)
      .contentType("application/json")

    request.send(client).flatMap { a =>
      println(a.statusText)
      a.body match {
        case Left(value) =>
          println(value)
          ZIO.succeed(())

        case Right(value) =>
          println(value)
          ZIO.succeed(())
      }
    }
  }

  private def executeGetById(getById: GetById): Task[Option[Document]] = {
    val request = basicRequest.get(
      uri"$baseUrl/${getById.index}/_doc/${getById.id}"
    )
    request
      .send(client)
      .flatMap { response =>
        response.body match {
          case Left(err) =>
            println(err)
            ZIO.succeed(Some(Document("""{"id": "lambdaworks", "count": 42}""")))
          case Right(body) =>
            body.fromJson[ElasticResponseClass] match {
              case Left(err) =>
                println(err)
                ZIO.succeed(Some(Document("""{"id": "lambdaworks", "count": 42}""")))
              case Right(elasticResponseClass) =>
                println(elasticResponseClass._source)
                ZIO.succeed(Some(Document("""{"id": "lambdaworks", "count": 42}""")))
            }
        }
      }
  }

}

private[elasticsearch] object HttpElasticExecutor {
  def create(config: ElasticConfig, client: SttpBackend[Task, Any]) =
    new HttpElasticExecutor(config, client)
}
