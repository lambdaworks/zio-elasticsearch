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
import zio.ZIO
import zio.elasticsearch._
import zio.elasticsearch.query.ElasticQuery
import zio.elasticsearch.request.{CreationOutcome, DeletionOutcome}
import zio.http._
import zio.http.model.Method
import zio.http.model.Status.{
  BadRequest => HttpBadRequest,
  Created => HttpCreated,
  NoContent => HttpNoContent,
  NotFound => HttpNotFound
}
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
              Response.json(ErrorResponse.fromReasons(s"Repository $id does not exist.").toJson).setStatus(HttpNotFound)
          }
          .orDie

      case req @ Method.POST -> BasePath =>
        req.body.asString
          .map(JsonCodec.JsonDecoder.decode[GitHubRepo](GitHubRepo.schema, _))
          .flatMap {
            case Left(e) =>
              ZIO.succeed(Response.json(ErrorResponse.fromReasons(e.message).toJson).setStatus(HttpBadRequest))
            case Right(repo) =>
              RepositoriesElasticsearch.create(repo).map {
                case CreationOutcome.Created =>
                  Response.json(repo.toJson).setStatus(HttpCreated)
                case CreationOutcome.AlreadyExists =>
                  Response.json("A repository with a given ID already exists.").setStatus(HttpBadRequest)
              }
          }
          .orDie

      case req @ Method.POST -> BasePath / "search" =>
        req.body.asString
          .map(JsonCodec.JsonDecoder.decode[Criteria](Criteria.schema, _))
          .flatMap {
            case Left(e) =>
              ZIO.succeed(Response.json(ErrorResponse.fromReasons(e.message).toJson).setStatus(HttpBadRequest))
            case Right(queryBody) =>
              req.url.queryParams
                .get("from")
                .map(_.head)
                .getOrElse("0")
                .toIntOption
                .toRight("The value of the from parameter is not an integer.")
                .flatMap { from =>
                  req.url.queryParams
                    .get("size")
                    .map(_.head)
                    .getOrElse("10")
                    .toIntOption
                    .toRight("The value of the size parameter is not an integer.")
                    .map { size =>
                      RepositoriesElasticsearch
                        .search(createElasticQuery(queryBody), from, size)
                        .map(repositories => Response.json(repositories.toJson))
                    }
                }
                .fold(
                  errorMessage =>
                    ZIO.succeed(
                      Response
                        .json(ErrorResponse.fromReasons(errorMessage).toJson)
                        .setStatus(HttpBadRequest)
                    ),
                  value => value
                )
          }
          .orDie

      case req @ Method.PUT -> BasePath / id =>
        req.body.asString
          .map(JsonCodec.JsonDecoder.decode[GitHubRepo](GitHubRepo.schema, _))
          .flatMap {
            case Left(e) =>
              ZIO.succeed(Response.json(ErrorResponse.fromReasons(e.message).toJson).setStatus(HttpBadRequest))
            case Right(repo) if repo.id != id =>
              ZIO.succeed(
                Response
                  .json(
                    ErrorResponse.fromReasons("The ID provided in the path does not match the ID from the body.").toJson
                  )
                  .setStatus(HttpBadRequest)
              )
            case Right(repo) =>
              (RepositoriesElasticsearch
                .upsert(id, repo.copy(id = id)) *> RepositoriesElasticsearch.findById(repo.organization, id)).map {
                case Some(updated) =>
                  Response.json(updated.toJson)
                case None =>
                  Response.json(ErrorResponse.fromReasons("Operation failed.").toJson).setStatus(HttpBadRequest)
              }
          }
          .orDie

      case Method.DELETE -> BasePath / organization / id =>
        RepositoriesElasticsearch
          .remove(organization, id)
          .map {
            case DeletionOutcome.Deleted =>
              Response.status(HttpNoContent)
            case DeletionOutcome.NotFound =>
              Response.json(ErrorResponse.fromReasons(s"Repository $id does not exist.").toJson).setStatus(HttpNotFound)
          }
          .orDie
    }

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
