package example.api

import example.{GitHubRepo, RepositoriesElasticsearch}
import zio.ZIO
import zio.elasticsearch.ElasticQuery.boolQuery
import zio.elasticsearch.{CreationOutcome, DeletionOutcome, ElasticQuery}
import zio.http._
import zio.http.model.Method
import zio.http.model.Status._
import zio.json.EncoderOps
import zio.schema.codec.JsonCodec

import CompoundOperator._
import FilterOperator._

object Repositories {

  private final val BasePath = !! / "api" / "repositories"

  final val Routes: Http[RepositoriesElasticsearch, Nothing, Request, Response] =
    Http.collectZIO[Request] {
      case Method.GET -> BasePath =>
        RepositoriesElasticsearch.findAll().map(repositories => Response.json(repositories.toJson)).orDie

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
                case CreationOutcome.Created =>
                  Response.json(repo.toJson).setStatus(Created)
                case CreationOutcome.AlreadyExists =>
                  Response.json("").setStatus(BadRequest)
              }
          }
          .orDie

      case req @ Method.POST -> BasePath / "search" =>
        req.body.asString
          .map(JsonCodec.JsonDecoder.decode[Criteria](Criteria.schema, _))
          .flatMap {
            case Left(e) =>
              ZIO.succeed(Response.json(ErrorResponse.fromReasons(e.message).toJson).setStatus(BadRequest))
            case Right(queryBody) =>
              RepositoriesElasticsearch
                .search(createElasticQuery(queryBody))
                .map(repositories => Response.json(repositories.toJson))
          }
          .orDie

      case req @ Method.PUT -> BasePath / id =>
        req.body.asString
          .map(JsonCodec.JsonDecoder.decode[GitHubRepo](GitHubRepo.schema, _))
          .flatMap {
            case Left(e) =>
              ZIO.succeed(Response.json(ErrorResponse.fromReasons(e.message).toJson).setStatus(BadRequest))
            case Right(repo) if repo.id == id =>
              ZIO.succeed(
                Response
                  .json(
                    ErrorResponse.fromReasons("The ID provided in the path does not match the ID from the body.").toJson
                  )
                  .setStatus(BadRequest)
              )
            case Right(repo) =>
              (RepositoriesElasticsearch
                .upsert(id, repo.copy(id = id)) *> RepositoriesElasticsearch.findById(repo.organization, id)).map {
                case Some(updated) => Response.json(updated.toJson)
                case None          => Response.json(ErrorResponse.fromReasons("Operation failed.").toJson).setStatus(BadRequest)
              }
          }
          .orDie

      case Method.DELETE -> BasePath / organization / id =>
        RepositoriesElasticsearch
          .remove(organization, id)
          .map {
            case DeletionOutcome.Deleted =>
              Response.status(NoContent)
            case DeletionOutcome.NotFound =>
              Response.json(ErrorResponse.fromReasons(s"Repository $id does not exist.").toJson).setStatus(NotFound)
          }
          .orDie
    }

  private def createElasticQuery(query: Criteria): ElasticQuery[_] =
    query match {
      case IntCriteria(field, operator, value) =>
        operator match {
          case GreaterThan =>
            ElasticQuery.range(field.toString).gt(value)
          case LessThan =>
            ElasticQuery.range(field.toString).lt(value)
          case EqualTo =>
            ElasticQuery.matches(field.toString, value)
        }
      case DateCriteria(field, operator, value) =>
        operator match {
          case GreaterThan =>
            ElasticQuery.range(field.toString).gt(value.toString)
          case LessThan =>
            ElasticQuery.range(field.toString).lt(value.toString)
          case EqualTo =>
            ElasticQuery.matches(field.toString, value.toString)
        }
      case CompoundCriteria(operator, filters) =>
        operator match {
          case And => boolQuery().must(filters.map(createElasticQuery): _*)
          case Or  => boolQuery().should(filters.map(createElasticQuery): _*)
        }
    }

}
