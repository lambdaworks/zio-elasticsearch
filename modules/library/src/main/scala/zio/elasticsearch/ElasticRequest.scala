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

package zio.elasticsearch

import zio.elasticsearch.Refresh.WithRefresh
import zio.elasticsearch.Routing.{Routing, WithRouting}
import zio.{Task, ZIO}

import scala.annotation.unused
import scala.language.implicitConversions

sealed trait ElasticRequest[+A, ERT <: ElasticRequestType] { self =>

  def execute: Task[A]

  final def map[B](f: A => Either[DecodingException, B]): ElasticRequest[B, ERT] = ElasticRequest.Map(self, f)

  final def refresh(value: Boolean)(implicit wr: WithRefresh[ERT]): ElasticRequest[A, ERT] =
    wr.withRefresh(request = self, value = value)

  final def refreshFalse(implicit wr: WithRefresh[ERT]): ElasticRequest[A, ERT] =
    wr.withRefresh(request = self, value = false)

  final def refreshTrue(implicit wr: WithRefresh[ERT]): ElasticRequest[A, ERT] =
    wr.withRefresh(request = self, value = true)

  final def routing(value: Routing)(implicit wr: WithRouting[ERT]): ElasticRequest[A, ERT] =
    wr.withRouting(request = self, routing = value)
}

object ElasticRequest {

  import ElasticRequestType._
  private[elasticsearch] final case class BulkableRequest private (request: ElasticRequest[_, _])

  object BulkableRequest {
    implicit def toBulkable[ERT <: ElasticRequestType](request: ElasticRequest[_, ERT])(implicit
      @unused ev: ERT <:< BulkableRequestType
    ): BulkableRequest =
      BulkableRequest(request)

    implicit def toBulkableList[ERT <: ElasticRequestType](requests: List[ElasticRequest[_, ERT]])(implicit
      @unused ev: ERT <:< BulkableRequestType
    ): List[BulkableRequest] =
      requests.map(BulkableRequest(_))
  }

  private[elasticsearch] abstract class BulkRequest(
    val requests: List[BulkableRequest],
    val index: Option[IndexName],
    val refresh: Boolean,
    val routing: Option[Routing]
  ) extends ElasticRequest[Unit, Bulk] {
    lazy val body: String = requests.flatMap { r =>
      // We use @unchecked to ignore 'pattern match not exhaustive' error since we guarantee that it will not happen
      // because these are only Bulkable Requests and other matches will not occur.
      (r.request: @unchecked) match {
        case r: CreateRequest =>
          List(getActionAndMeta("create", List(("_index", Some(r.index)), ("routing", r.routing))), r.document.json)
        case r: CreateWithIdRequest =>
          List(
            getActionAndMeta("create", List(("_index", Some(r.index)), ("_id", Some(r.id)), ("routing", r.routing))),
            r.document.json
          )
        case r: CreateOrUpdateRequest =>
          List(
            getActionAndMeta("index", List(("_index", Some(r.index)), ("_id", Some(r.id)), ("routing", r.routing))),
            r.document.json
          )
        case r: DeleteByIdRequest =>
          List(getActionAndMeta("delete", List(("_index", Some(r.index)), ("_id", Some(r.id)), ("routing", r.routing))))
      }
    }.mkString(start = "", sep = "\n", end = "\n")
  }

  private[elasticsearch] abstract class CreateRequest(
    val index: IndexName,
    val document: Document,
    val refresh: Boolean,
    val routing: Option[Routing]
  ) extends ElasticRequest[DocumentId, Create]

  private[elasticsearch] abstract class CreateWithIdRequest(
    val index: IndexName,
    val id: DocumentId,
    val document: Document,
    val refresh: Boolean,
    val routing: Option[Routing]
  ) extends ElasticRequest[CreationOutcome, CreateWithId]

  private[elasticsearch] abstract class CreateIndexRequest(
    val name: IndexName,
    val definition: Option[String]
  ) extends ElasticRequest[CreationOutcome, CreateIndex]

  private[elasticsearch] abstract class CreateOrUpdateRequest(
    val index: IndexName,
    val id: DocumentId,
    val document: Document,
    val refresh: Boolean,
    val routing: Option[Routing]
  ) extends ElasticRequest[Unit, Upsert]

  private[elasticsearch] abstract class DeleteByIdRequest(
    val index: IndexName,
    val id: DocumentId,
    val refresh: Boolean,
    val routing: Option[Routing]
  ) extends ElasticRequest[DeletionOutcome, DeleteById]

  private[elasticsearch] abstract class DeleteByQueryRequest(
    val index: IndexName,
    val query: ElasticQuery[_],
    val refresh: Boolean,
    val routing: Option[Routing]
  ) extends ElasticRequest[DeletionOutcome, DeleteByQuery]

  private[elasticsearch] abstract class DeleteIndexRequest(val name: IndexName)
      extends ElasticRequest[DeletionOutcome, DeleteIndex]

  private[elasticsearch] abstract class ExistsRequest(
    val index: IndexName,
    val id: DocumentId,
    val routing: Option[Routing]
  ) extends ElasticRequest[Boolean, Exists]

  private[elasticsearch] abstract class GetByIdRequest(
    val index: IndexName,
    val id: DocumentId,
    val routing: Option[Routing]
  ) extends ElasticRequest[Option[Document], GetById]

  private[elasticsearch] abstract class GetByQueryRequest(
    val index: IndexName,
    val query: ElasticQuery[_],
    val routing: Option[Routing]
  ) extends ElasticRequest[ElasticQueryResponse, GetByQuery]

  private[elasticsearch] final case class Map[A, B, ERT <: ElasticRequestType](
    request: ElasticRequest[A, ERT],
    mapper: A => Either[DecodingException, B]
  ) extends ElasticRequest[B, ERT] {
    def execute: Task[B] = request.execute.flatMap(a => ZIO.fromEither(mapper(a)))
  }

  private def getActionAndMeta(requestType: String, parameters: List[(String, Any)]): String =
    parameters.collect { case (name, Some(value)) => s""""$name" : "${value.toString}"""" }
      .mkString(s"""{ "$requestType" : { """, ", ", " } }")

}

sealed trait ElasticRequestType

sealed trait BulkableRequestType extends ElasticRequestType

object ElasticRequestType {
  sealed trait Bulk          extends ElasticRequestType
  sealed trait CreateIndex   extends ElasticRequestType
  sealed trait Create        extends BulkableRequestType
  sealed trait CreateWithId  extends BulkableRequestType
  sealed trait DeleteById    extends BulkableRequestType
  sealed trait DeleteByQuery extends ElasticRequestType
  sealed trait DeleteIndex   extends ElasticRequestType
  sealed trait Exists        extends ElasticRequestType
  sealed trait GetById       extends ElasticRequestType
  sealed trait GetByQuery    extends ElasticRequestType
  sealed trait Upsert        extends BulkableRequestType
}

sealed trait CreationOutcome

case object Created       extends CreationOutcome
case object AlreadyExists extends CreationOutcome

sealed trait DeletionOutcome

case object Deleted  extends DeletionOutcome
case object NotFound extends DeletionOutcome
