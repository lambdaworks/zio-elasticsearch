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

import zio.elasticsearch.Routing.Routing
import zio.schema.Schema

trait HasRefresh[R <: HasRefresh[R]] {
  def refresh(value: Boolean): R

  def refreshFalse: R

  def refreshTrue: R
}

trait HasRouting[R <: HasRouting[R]] {
  def routing(value: Routing): R
}

sealed trait BulkableRequest[A] extends ElasticRequest[A]

sealed trait ElasticRequest[A]

object ElasticRequest {

  def aggregate(index: IndexName, aggregation: ElasticAggregation): AggregationRequest =
    Aggregation(index = index, aggregation = aggregation)

  def bulk(requests: BulkableRequest[_]*): BulkRequest =
    Bulk.of(requests = requests: _*)

  def create[A: Schema](index: IndexName, doc: A): CreateRequest =
    Create(index = index, document = Document.from(doc), refresh = None, routing = None)

  def create[A: Schema](index: IndexName, id: DocumentId, doc: A): CreateWithIdRequest =
    CreateWithId(index = index, id = id, document = Document.from(doc), refresh = None, routing = None)

  def createIndex(name: IndexName): CreateIndexRequest =
    CreateIndex(name = name, definition = None)

  def createIndex(name: IndexName, definition: String): CreateIndexRequest =
    CreateIndex(name = name, definition = Some(definition))

  def deleteById(index: IndexName, id: DocumentId): DeleteByIdRequest =
    DeleteById(index = index, id = id, refresh = None, routing = None)

  def deleteByQuery(index: IndexName, query: ElasticQuery[_]): DeleteByQueryRequest =
    DeleteByQuery(index = index, query = query, refresh = None, routing = None)

  def deleteIndex(name: IndexName): DeleteIndexRequest =
    DeleteIndex(name = name)

  def exists(index: IndexName, id: DocumentId): ExistRequest =
    Exists(index = index, id = id, routing = None)

  def getById(index: IndexName, id: DocumentId): GetByIdRequest =
    GetById(index = index, id = id, refresh = None, routing = None)

  def search(index: IndexName, query: ElasticQuery[_]): SearchRequest =
    Search(index = index, query = query, routing = None)

  def searchWithAggregation(
    index: IndexName,
    query: ElasticQuery[_],
    aggregation: ElasticAggregation
  ): SearchWithAggregationRequest =
    SearchWithAggregation(index = index, query = query, aggregation = aggregation)

  def upsert[A: Schema](index: IndexName, id: DocumentId, doc: A): CreateOrUpdateRequest =
    CreateOrUpdate(index = index, id = id, document = Document.from(doc), refresh = None, routing = None)

  sealed trait AggregationRequest extends ElasticRequest[AggregationResult]

  private[elasticsearch] final case class Aggregation(index: IndexName, aggregation: ElasticAggregation)
      extends AggregationRequest

  sealed trait BulkRequest extends ElasticRequest[Unit] with HasRefresh[BulkRequest] with HasRouting[BulkRequest]

  private[elasticsearch] final case class Bulk(
    requests: List[BulkableRequest[_]],
    index: Option[IndexName],
    refresh: Option[Boolean],
    routing: Option[Routing]
  ) extends BulkRequest { self =>
    def refresh(value: Boolean): BulkRequest =
      self.copy(refresh = Some(value))

    def refreshFalse: BulkRequest =
      refresh(false)

    def refreshTrue: BulkRequest =
      refresh(true)

    def routing(value: Routing): BulkRequest =
      self.copy(routing = Some(value))

    lazy val body: String = requests.flatMap { r =>
      r match {
        case Create(index, document, _, maybeRouting) =>
          List(getActionAndMeta("create", List(("_index", Some(index)), ("routing", maybeRouting))), document.json)
        case CreateWithId(index, id, document, _, maybeRouting) =>
          List(
            getActionAndMeta("create", List(("_index", Some(index)), ("_id", Some(id)), ("routing", maybeRouting))),
            document.json
          )
        case CreateOrUpdate(index, id, document, _, maybeRouting) =>
          List(
            getActionAndMeta("index", List(("_index", Some(index)), ("_id", Some(id)), ("routing", maybeRouting))),
            document.json
          )
        case DeleteById(index, id, _, maybeRouting) =>
          List(getActionAndMeta("delete", List(("_index", Some(index)), ("_id", Some(id)), ("routing", maybeRouting))))
      }
    }.mkString(start = "", sep = "\n", end = "\n")
  }

  object Bulk {
    def of(requests: BulkableRequest[_]*): Bulk =
      Bulk(requests = requests.toList, index = None, refresh = None, routing = None)
  }

  sealed trait CreateRequest
      extends BulkableRequest[DocumentId]
      with HasRefresh[CreateRequest]
      with HasRouting[CreateRequest]

  private[elasticsearch] final case class Create(
    index: IndexName,
    document: Document,
    refresh: Option[Boolean],
    routing: Option[Routing]
  ) extends CreateRequest { self =>
    def refresh(value: Boolean): CreateRequest =
      self.copy(refresh = Some(value))

    def refreshFalse: CreateRequest =
      refresh(false)

    def refreshTrue: CreateRequest =
      refresh(true)

    def routing(value: Routing): CreateRequest =
      self.copy(routing = Some(value))
  }

  sealed trait CreateWithIdRequest
      extends BulkableRequest[CreationOutcome]
      with HasRefresh[CreateWithIdRequest]
      with HasRouting[CreateWithIdRequest]

  private[elasticsearch] final case class CreateWithId(
    index: IndexName,
    id: DocumentId,
    document: Document,
    refresh: Option[Boolean],
    routing: Option[Routing]
  ) extends CreateWithIdRequest { self =>
    def refresh(value: Boolean): CreateWithIdRequest =
      self.copy(refresh = Some(value))

    def refreshFalse: CreateWithIdRequest =
      refresh(false)

    def refreshTrue: CreateWithIdRequest =
      refresh(true)

    def routing(value: Routing): CreateWithIdRequest =
      self.copy(routing = Some(value))
  }

  sealed trait CreateIndexRequest extends ElasticRequest[CreationOutcome]

  private[elasticsearch] final case class CreateIndex(
    name: IndexName,
    definition: Option[String]
  ) extends CreateIndexRequest

  sealed trait CreateOrUpdateRequest
      extends BulkableRequest[Unit]
      with HasRefresh[CreateOrUpdateRequest]
      with HasRouting[CreateOrUpdateRequest]

  private[elasticsearch] final case class CreateOrUpdate(
    index: IndexName,
    id: DocumentId,
    document: Document,
    refresh: Option[Boolean],
    routing: Option[Routing]
  ) extends CreateOrUpdateRequest { self =>
    def refresh(value: Boolean): CreateOrUpdateRequest =
      self.copy(refresh = Some(value))

    def refreshFalse: CreateOrUpdateRequest =
      refresh(false)

    def refreshTrue: CreateOrUpdateRequest =
      refresh(true)

    def routing(value: Routing): CreateOrUpdateRequest =
      self.copy(routing = Some(value))
  }

  sealed trait DeleteByIdRequest
      extends BulkableRequest[DeletionOutcome]
      with HasRefresh[DeleteByIdRequest]
      with HasRouting[DeleteByIdRequest]

  private[elasticsearch] final case class DeleteById(
    index: IndexName,
    id: DocumentId,
    refresh: Option[Boolean],
    routing: Option[Routing]
  ) extends DeleteByIdRequest { self =>
    def refresh(value: Boolean): DeleteByIdRequest =
      self.copy(refresh = Some(value))

    def refreshFalse: DeleteByIdRequest =
      refresh(false)

    def refreshTrue: DeleteByIdRequest =
      refresh(true)

    def routing(value: Routing): DeleteByIdRequest =
      self.copy(routing = Some(value))
  }

  sealed trait DeleteByQueryRequest
      extends ElasticRequest[DeletionOutcome]
      with HasRefresh[DeleteByQueryRequest]
      with HasRouting[DeleteByQueryRequest]

  private[elasticsearch] final case class DeleteByQuery(
    index: IndexName,
    query: ElasticQuery[_],
    refresh: Option[Boolean],
    routing: Option[Routing]
  ) extends DeleteByQueryRequest { self =>
    def refresh(value: Boolean): DeleteByQueryRequest =
      self.copy(refresh = Some(value))

    def refreshFalse: DeleteByQueryRequest =
      refresh(false)

    def refreshTrue: DeleteByQueryRequest =
      refresh(true)

    def routing(value: Routing): DeleteByQueryRequest =
      self.copy(routing = Some(value))
  }

  sealed trait DeleteIndexRequest extends ElasticRequest[DeletionOutcome]

  final case class DeleteIndex(name: IndexName) extends DeleteIndexRequest

  sealed trait ExistRequest extends ElasticRequest[Boolean] with HasRouting[ExistRequest]

  private[elasticsearch] final case class Exists(
    index: IndexName,
    id: DocumentId,
    routing: Option[Routing]
  ) extends ExistRequest { self =>
    def routing(value: Routing): ExistRequest =
      self.copy(routing = Some(value))
  }

  sealed trait GetByIdRequest
      extends ElasticRequest[GetResult]
      with HasRefresh[GetByIdRequest]
      with HasRouting[GetByIdRequest]

  private[elasticsearch] final case class GetById(
    index: IndexName,
    id: DocumentId,
    refresh: Option[Boolean],
    routing: Option[Routing]
  ) extends GetByIdRequest { self =>
    def refresh(value: Boolean): GetByIdRequest =
      self.copy(refresh = Some(value))

    def refreshFalse: GetByIdRequest =
      refresh(false)

    def refreshTrue: GetByIdRequest =
      refresh(true)

    def routing(value: Routing): GetByIdRequest =
      self.copy(routing = Some(value))
  }

  sealed trait SearchRequest extends ElasticRequest[SearchResult]

  private[elasticsearch] final case class Search(
    index: IndexName,
    query: ElasticQuery[_],
    routing: Option[Routing]
  ) extends SearchRequest

  sealed trait SearchWithAggregationRequest extends ElasticRequest[SearchWithAggregationsResult]

  private[elasticsearch] final case class SearchWithAggregation(
    index: IndexName,
    query: ElasticQuery[_],
    aggregation: ElasticAggregation
  ) extends SearchWithAggregationRequest

  private def getActionAndMeta(requestType: String, parameters: List[(String, Any)]): String =
    parameters.collect { case (name, Some(value)) => s""""$name" : "${value.toString}"""" }
      .mkString(s"""{ "$requestType" : { """, ", ", " } }")

}

sealed abstract class CreationOutcome

case object AlreadyExists extends CreationOutcome
case object Created       extends CreationOutcome

sealed abstract class DeletionOutcome

case object Deleted  extends DeletionOutcome
case object NotFound extends DeletionOutcome
