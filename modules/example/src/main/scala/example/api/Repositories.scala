package example.api

import example.{RepositoriesElasticsearch, Repository}
import zio.ZIO
import zio.http._
import zio.http.model.Status.{BadRequest, Created}
import zio.http.model.{Method, Status}
import zio.json.EncoderOps
import zio.schema.codec.JsonCodec
import zio.Console

object Repositories {

  private final val BasePath = !! / "api" / "repositories"

  final val Routes: Http[Any, Nothing, Request, Response] =
    Http.collectZIO[Request] {
      case Method.GET -> !! / "health" =>
        ZIO.succeed(Response.text("up"))

      case Method.GET -> BasePath =>
        ZIO.succeed(Response.text("list of repositories"))

      case Method.DELETE -> BasePath / id =>
        ZIO.succeed(Response.text("Deleting repository"))

      case Method.GET -> BasePath / id =>
        (for {
          es         <- ZIO.service[RepositoriesElasticsearch]
          repository <- es.one(id)
        } yield repository).map {
          case Some(r) => Response.json(JsonCodec.JsonEncoder.encode(Repository.schema, r).toJson)
          case None    => Response.status(Status.NotFound)
        }.provide(RepositoriesElasticsearch.live).orDie

      case req @ Method.POST -> BasePath =>
        (for {
          data <- req.body.asString
          repo <- JsonCodec.JsonDecoder.decode[Repository](Repository.schema, data)
          es   <- ZIO.service[RepositoriesElasticsearch]
          id   <- es.create(repo)
        } yield id).flatMap {
          case Some(id) => Console.printLine(s"Created with $id").as(Response.status(Created))
          case None     => ZIO.succeed(Response.status(BadRequest))
        }.provide(RepositoriesElasticsearch.live).orDie

      case Method.PUT -> BasePath =>
        ZIO.succeed(Response.text("Upsert repository"))
    }

}
