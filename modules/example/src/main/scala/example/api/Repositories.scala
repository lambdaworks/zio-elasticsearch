package example.api

import example.{RepositoriesElasticsearch, Repository}
import zio.ZIO
import zio.elasticsearch.ElasticExecutor
import zio.http._
import zio.http.model.{Method, Status}
import zio.json.EncoderOps
import zio.schema.codec.JsonCodec

final class Repositories(es: RepositoriesElasticsearch) {

  private final val BasePath = !! / "api" / "repositories"

  final val Routes: Http[ElasticExecutor, Nothing, Request, Response] =
    Http.collectZIO[Request] {
      case Method.GET -> !! / "health" =>
        ZIO.succeed(Response.text("up"))

      case Method.GET -> BasePath =>
        ZIO.succeed(Response.text("list of repositories"))

      case Method.DELETE -> BasePath / id =>
        ZIO.succeed(Response.text(s"Deleting repository $id"))

      case Method.GET -> BasePath / id =>
        (for {
          repository <- es.one(id)
        } yield repository).map {
          case Some(r) => Response.json(JsonCodec.JsonEncoder.encode(Repository.schema, r).toJson)
          case None    => Response.status(Status.NotFound)
        }.orDie

//      case req @ Method.POST -> BasePath =>
//        (for {
//          data <- req.body.asString
//          repo <- JsonCodec.JsonDecoder.decode[Repository](Repository.schema, data)
//          es   <- ZIO.service[RepositoriesElasticsearch]
//          id   <- es.create(repo)
//        } yield id).flatMap {
//          case Some(id) => Console.printLine(s"Created with $id").as(Response.status(Created))
//          case None     => ZIO.succeed(Response.status(BadRequest))
//        }.orDie

      case Method.PUT -> BasePath =>
        ZIO.succeed(Response.text("Upsert repository"))
    }

}
