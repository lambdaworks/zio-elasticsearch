package zio.elasticsearch

import sttp.client3._
import sttp.model.Uri
import zio.elasticsearch.ElasticRequest.{Constructor, GetById, Map, Put}
import zio.json.DecoderOps
import zio.{Task, ZIO}

private[elasticsearch] final class HttpElasticExecutor private (config: ElasticConfig, client: SttpBackend[Task, Any])
    extends ElasticExecutor {

  private val uri = Uri(config.host, config.port)

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
//    val request = basicRequest
//      .post(
//        uri"$baseUrl/${put.index}/_doc/"
//      )
//      .body(putBody)
//      .contentType("application/json")
//
//    request.send(client).flatMap { a =>
//      println(a.statusText)
//      a.body match {
//        case Left(value) =>
//          println(value)
//          ZIO.succeed(())
//
//        case Right(value) =>
//          println(value)
//          ZIO.succeed(())
//      }
//    }
    println(put)
    ZIO.unit
  }

  private def executeGetById(getById: GetById): Task[Option[Document]] = {
    val u =
      uri.withPath(getById.index.name, "_doc", getById.id.value).withParam("routing", getById.routing.map(_.value))
    basicRequest.get(u).send(client).map {
      case Response(Right(body), _, _, _, _, _) =>
        body.fromJson[ElasticResponse] match {
          case Right(res) if res.found => Some(Document(res.source.toJson))
          case _                       => None
        }

      case Response(Left(_), _, _, _, _, _) => None
    }
  }

}

private[elasticsearch] object HttpElasticExecutor {
  def create(config: ElasticConfig, client: SttpBackend[Task, Any]) =
    new HttpElasticExecutor(config, client)
}
