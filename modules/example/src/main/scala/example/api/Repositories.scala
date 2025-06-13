/*
 * Copyright 2022 LambdaWorks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package example.api

import example.{GitHubRepo, RepositoriesElasticsearch}
import zio.elasticsearch._
import zio.elasticsearch.query.ElasticQuery
import zio.elasticsearch.request.{CreationOutcome, DeletionOutcome}
import zio.http.Status.{
  BadRequest => HttpBadRequest,
  Created => HttpCreated,
  NoContent => HttpNoContent,
  NotFound => HttpNotFound
}
import zio.http.{Method, _}
import zio.json.EncoderOps
import zio.schema.Schema
import zio.schema.codec.JsonCodec.{Configuration => JsonCodecConfig, JsonDecoder}
import zio.{Chunk, ZIO}

import CompoundOperator._
import FilterOperator._

object Repositories {

  private final val BasePath = Root / "api" / "repositories"

  final val routes: Routes[RepositoriesElasticsearch, Nothing] =
    Routes(
      Method.GET / BasePath -> handler(
        RepositoriesElasticsearch.findAll().map(repositories => Response.json(repositories.toJson))
      ).orDie,
      Method.GET / BasePath / string("organization") / string("id") -> handler {
        (organization: String, id: String, _: Request) =>
          RepositoriesElasticsearch
            .findById(organization, id)
            .map {
              case Some(r) =>
                Response.json(r.toJson)
              case None =>
                Response.json(ErrorResponse.fromReasons(s"Repository $id does not exist.").toJson).status(HttpNotFound)
            }
      }.orDie,
      Method.POST / BasePath -> handler { (req: Request) =>
        req.body.asString
          .map(JsonDecoder.decode[GitHubRepo](GitHubRepo.schema, _, JsonCodecConfig.default))
          .flatMap {
            case Left(e) =>
              ZIO.succeed(Response.json(ErrorResponse.fromReasons(e.message).toJson).status(HttpBadRequest))
            case Right(repo) =>
              RepositoriesElasticsearch.create(repo).map {
                case CreationOutcome.Created =>
                  Response.json(repo.toJson).status(HttpCreated)
                case CreationOutcome.AlreadyExists =>
                  Response.json("A repository with a given ID already exists.").status(HttpBadRequest)
              }
          }
      }.orDie,
      Method.POST / BasePath / string("organization") / "bulk-upsert" -> handler {
        (organization: String, req: Request) =>
          req.body.asString
            .map(JsonDecoder.decode[Chunk[GitHubRepo]](Schema.chunk(GitHubRepo.schema), _, JsonCodecConfig.default))
            .flatMap {
              case Left(jsonError) =>
                ZIO.succeed(Response.json(ErrorResponse.fromReasons(jsonError.message).toJson).status(HttpBadRequest))
              case Right(repositories) =>
                RepositoriesElasticsearch
                  .upsertBulk(organization, repositories)
                  .map(_ => Response.status(HttpNoContent))
                  .catchAll(e =>
                    ZIO.succeed(
                      Response
                        .json(ErrorResponse.fromReasons(s"Bulk operation failed: ${e.getMessage}").toJson)
                        .status(HttpBadRequest)
                    )
                  )
            }
      }.orDie,
      Method.POST / BasePath / "search" -> handler { (req: Request) =>
        req.body.asString
          .map(JsonDecoder.decode[Criteria](Criteria.schema, _, JsonCodecConfig.default))
          .flatMap {
            case Left(e) =>
              ZIO.succeed(Response.json(ErrorResponse.fromReasons(e.message).toJson).status(HttpBadRequest))
            case Right(queryBody) =>
              RepositoriesElasticsearch
                .search(createElasticQuery(queryBody), req.offset, req.limit)
                .map(repositories => Response.json(repositories.toJson))
          }
      }.orDie,
      Method.PUT / BasePath / string("id") -> handler { (id: String, req: Request) =>
        req.body.asString
          .map(JsonDecoder.decode[GitHubRepo](GitHubRepo.schema, _, JsonCodecConfig.default))
          .flatMap {
            case Left(e) =>
              ZIO.succeed(Response.json(ErrorResponse.fromReasons(e.message).toJson).status(HttpBadRequest))
            case Right(repo) if repo.id != id =>
              ZIO.succeed(
                Response
                  .json(
                    ErrorResponse.fromReasons("The ID provided in the path does not match the ID from the body.").toJson
                  )
                  .status(HttpBadRequest)
              )
            case Right(repo) =>
              (RepositoriesElasticsearch
                .upsert(id, repo.copy(id = id)) *> RepositoriesElasticsearch.findById(repo.organization, id)).map {
                case Some(updated) =>
                  Response.json(updated.toJson)
                case None =>
                  Response.json(ErrorResponse.fromReasons("Operation failed.").toJson).status(HttpBadRequest)
              }
          }
      }.orDie,
      Method.DELETE / BasePath / string("organization") / string("id") -> handler {
        (organization: String, id: String, _: Request) =>
          RepositoriesElasticsearch
            .remove(organization, id)
            .map {
              case DeletionOutcome.Deleted =>
                Response.status(HttpNoContent)
              case DeletionOutcome.NotFound =>
                Response.json(ErrorResponse.fromReasons(s"Repository $id does not exist.").toJson).status(HttpNotFound)
            }
      }.orDie
    )

  private def createElasticQuery(query: Criteria): ElasticQuery[Any] =
    query match {
      case CompoundCriteria(operator, filters) =>
        operator match {
          case And => ElasticQuery.must(filters.map(createElasticQuery): _*)
          case Not => ElasticQuery.mustNot(filters.map(createElasticQuery): _*)
          case Or  => ElasticQuery.should(filters.map(createElasticQuery): _*)
        }
      case DateCriteria(field, operator, value) =>
        operator match {
          case GreaterThan => ElasticQuery.range(field.toString).gt(value.toString)
          case LessThan    => ElasticQuery.range(field.toString).lt(value.toString)
          case EqualTo     => ElasticQuery.matches(field.toString, value.toString)
        }
      case IntCriteria(field, operator, value) =>
        operator match {
          case GreaterThan => ElasticQuery.range(field.toString).gt(value)
          case LessThan    => ElasticQuery.range(field.toString).lt(value)
          case EqualTo     => ElasticQuery.matches(field.toString, value)
        }
      case StringCriteria(field, operator, value) =>
        operator match {
          case StringFilterOperator.Contains   => ElasticQuery.contains(field.toString, value)
          case StringFilterOperator.StartsWith => ElasticQuery.startsWith(field.toString, value)
          case StringFilterOperator.Pattern    => ElasticQuery.wildcard(field.toString, value)
        }
    }

}
