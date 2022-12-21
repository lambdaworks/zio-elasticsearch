package example.api

import example.{GitHubRepo, RepositoriesElasticsearch}
import zio.ZIO
import zio.elasticsearch.{DocumentId, ElasticExecutor}
import zio.http._
import zio.http.model.Method
import zio.http.model.Status._
import zio.json.EncoderOps
import zio.schema.codec.JsonCodec

object Repositories {

  private final val BasePath = !! / "api" / "repositories"

  final val Routes: Http[ElasticExecutor with RepositoriesElasticsearch, Nothing, Request, Response] =
    Http.collectZIO[Request] {
      case Method.GET -> BasePath =>
        ZIO.succeed(Response.text("TODO: Get a list of repositories").setStatus(NotImplemented))

      case Method.GET -> BasePath / organization / id =>
        RepositoriesElasticsearch
          .findById(organization, id)
          .map {
            case Some(r) =>
              Response.json(r.toJson)
            case None =>
              Response.json(ErrorResponse.fromReasons(s"Repository $id does not exist.").toJson).setStatus(NotFound)
          }
          .orDie

      case req @ Method.POST -> BasePath =>
        req.body.asString
          .map(JsonCodec.JsonDecoder.decode[GitHubRepo](GitHubRepo.schema, _))
          .flatMap {
            case Left(e) =>
              ZIO.succeed(Response.json(ErrorResponse.fromReasons(e.message).toJson).setStatus(BadRequest))
            case Right(repo) =>
              RepositoriesElasticsearch.create(repo).map {
                case Some(id) =>
                  Response.json(repo.copy(id = Some(DocumentId.unwrap(id))).toJson).setStatus(Created)
                case None =>
                  Response.json(ErrorResponse.fromReasons("Failed to create repository.").toJson).setStatus(BadRequest)
              }
          }
          .orDie

      case req @ Method.PUT -> BasePath / id =>
        req.body.asString
          .map(JsonCodec.JsonDecoder.decode[GitHubRepo](GitHubRepo.schema, _))
          .flatMap {
            case Left(e) =>
              ZIO.succeed(Response.json(ErrorResponse.fromReasons(e.message).toJson).setStatus(BadRequest))
            case Right(repo) if repo.id.exists(_ != id) =>
              ZIO.succeed(
                Response
                  .json(
                    ErrorResponse.fromReasons("The ID provided in the path does not match the ID from the body.").toJson
                  )
                  .setStatus(BadRequest)
              )
            case Right(repo) =>
              (RepositoriesElasticsearch
                .upsert(id, repo.copy(id = Some(id))) *> RepositoriesElasticsearch.findById(
                repo.organization,
                id
              )).map {
                case Some(updated) => Response.json(updated.toJson)
                case None          => Response.json(ErrorResponse.fromReasons("Operation failed.").toJson).setStatus(BadRequest)
              }
          }
          .orDie

      case Method.DELETE -> BasePath / organization / id =>
        RepositoriesElasticsearch
          .remove(organization, id)
          .map {
            case Right(_) =>
              Response.status(NoContent)
            case Left(_) =>
              Response.json(ErrorResponse.fromReasons(s"Repository $id does not exist.").toJson).setStatus(NotFound)
          }
          .orDie
    }

}
