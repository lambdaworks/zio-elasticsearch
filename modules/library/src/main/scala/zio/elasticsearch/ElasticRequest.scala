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

import zio.elasticsearch.ElasticPrimitive.ElasticPrimitiveOps
import zio.elasticsearch.aggregation.ElasticAggregation
import zio.elasticsearch.highlights.Highlights
import zio.elasticsearch.query.ElasticQuery
import zio.elasticsearch.query.sort.Sort
import zio.elasticsearch.request._
import zio.elasticsearch.result.{
  AggregationResult,
  GetResult,
  SearchAndAggregateResult,
  SearchResult,
  UpdateByQueryResult
}
import zio.elasticsearch.script.Script
import zio.json.ast.Json
import zio.json.ast.Json.{Arr, Obj}
import zio.schema.Schema

sealed trait BulkableRequest[A] extends ElasticRequest[A]

sealed trait ElasticRequest[A]

object ElasticRequest {

  /**
   * Constructs an instance of [[AggregateRequest]] using the specified parameters.
   *
   * @param index
   *   the name of the Elasticsearch index to aggregate on
   * @param aggregation
   *   the desired [[ElasticAggregation]] to perform
   * @return
   *   an instance of [[AggregateRequest]] that represents the aggregation to be performed.
   */
  final def aggregate(index: IndexName, aggregation: ElasticAggregation): AggregateRequest =
    Aggregate(index = index, aggregation = aggregation)

  /**
   * Constructs an instance of [[BulkRequest]] using the specified requests.
   *
   * @param requests
   *   a list of requests that will be executed as a bulk
   * @return
   *   an instance of [[BulkRequest]] that represents the bulk operation to be performed.
   */
  final def bulk(requests: BulkableRequest[_]*): BulkRequest =
    Bulk.of(requests = requests: _*)

  /**
   * Constructs an instance of [[CountRequest]] for whole specified index.
   *
   * @param index
   *   the name of the index to count documents from
   * @return
   *   an instance of [[CountRequest]] that represents the count operation to be performed.
   */
  final def count(index: IndexName): CountRequest =
    Count(index = index, query = None, routing = None)

  /**
   * Constructs an instance of [[CountRequest]] for counting documents satisfy the query.
   *
   * @param index
   *   the name of the Elasticsearch index to count documents from
   * @param query
   *   the [[ElasticQuery]] object to query documents that will be counted
   * @return
   *   an instance of [[CountRequest]] that represents the count operation to be performed.
   */
  final def count(index: IndexName, query: ElasticQuery[_]): CountRequest =
    Count(index = index, query = Some(query), routing = None)

  /**
   * Constructs an instance of [[CreateRequest]] used for creating a document in the specified index.
   *
   * @param index
   *   the name of the index to create the document in
   * @param doc
   *   the document to be created, represented by an instance of type `A`
   * @tparam A
   *   the type of the document to be created. An implicit `Schema` instance must be in scope for this type
   * @return
   *   an instance of [[CreateRequest]] that represents the create operation to be performed.
   */
  final def create[A: Schema](index: IndexName, doc: A): CreateRequest =
    Create(index = index, document = Document.from(doc), refresh = None, routing = None)

  /**
   * Constructs an instance of [[CreateWithIdRequest]] used for creating a document with specified ID in the specified
   * index.
   *
   * @param index
   *   the name of the index to create the document in
   * @param id
   *   the ID of the new document
   * @param doc
   *   the document to be created, represented by an instance of type `A`
   * @tparam A
   *   the type of the document to be created. An implicit `Schema` instance must be in scope for this type
   * @return
   *   an instance of [[CreateRequest]] that represents the create with id operation to be performed.
   */
  final def create[A: Schema](index: IndexName, id: DocumentId, doc: A): CreateWithIdRequest =
    CreateWithId(index = index, id = id, document = Document.from(doc), refresh = None, routing = None)

  /**
   * Constructs an instance of [[CreateIndexRequest]] used for creating an empty index.
   *
   * @param name
   *   the name of the index to be created
   * @return
   *   an instance of [[CreateIndexRequest]] that represents the create index operation to be performed.
   */
  final def createIndex(name: IndexName): CreateIndexRequest =
    CreateIndex(name = name, definition = None)

  /**
   * Constructs an instance of [[CreateIndexRequest]] used for creating an index with a specified definition.
   *
   * @param name
   *   the name of the index to be created
   * @param definition
   *   the settings for the index
   * @return
   *   an instance of [[CreateIndexRequest]] that represents the create index operation to be performed.
   */
  final def createIndex(name: IndexName, definition: String): CreateIndexRequest =
    CreateIndex(name = name, definition = Some(definition))

  /**
   * Constructs an instance of [[DeleteByIdRequest]] used for deleting a document from the specified index by specified
   * ID.
   *
   * @param index
   *   the name of the index to delete the document from
   * @param id
   *   the ID of the document to be deleted
   * @return
   *   an instance of [[DeleteByIdRequest]] that represents delete by id operation to be performed.
   */
  final def deleteById(index: IndexName, id: DocumentId): DeleteByIdRequest =
    DeleteById(index = index, id = id, refresh = None, routing = None)

  /**
   * Constructs an instance of [[DeleteByQueryRequest]] used for deleting documents from the specified index that
   * satisfy specified query.
   *
   * @param index
   *   the name of the index to delete documents from
   * @param query
   *   the [[ElasticQuery]] object to query documents which will be deleted
   * @return
   *   an instance of [[DeleteByQueryRequest]] that represents delete by query operation to be performed.
   */
  final def deleteByQuery(index: IndexName, query: ElasticQuery[_]): DeleteByQueryRequest =
    DeleteByQuery(index = index, query = query, refresh = None, routing = None)

  /**
   * Constructs an instance of [[DeleteIndexRequest]] used for deleting an index by specified name.
   *
   * @param name
   *   the name of the index to be deleted
   * @return
   *   an instance of [[DeleteIndexRequest]] that represents delete index operation to be performed.
   */
  final def deleteIndex(name: IndexName): DeleteIndexRequest =
    DeleteIndex(name = name)

  /**
   * Constructs an instance of [[ExistRequest]] used for checking whether document exists.
   *
   * @param index
   *   the name of the index where the document may be located
   * @param id
   *   the ID of the document to check for existence
   * @return
   *   an instance of [[ExistRequest]] that represents exists operation to be performed.
   */
  final def exists(index: IndexName, id: DocumentId): ExistRequest =
    Exists(index = index, id = id, routing = None)

  /**
   * Constructs an instance of [[GetByIdRequest]] used for retrieving the document from specified index, by specified
   * ID.
   *
   * @param index
   *   the name of the index where the document is located
   * @param id
   *   the ID of the document to retrieve
   * @return
   *   an instance of [[GetByIdRequest]] that represents get by id operation to be performed.
   */
  final def getById(index: IndexName, id: DocumentId): GetByIdRequest =
    GetById(index = index, id = id, refresh = None, routing = None)

  /**
   * Constructs an instance of [[SearchRequest]] using the specified parameters.
   *
   * @param index
   *   the name of the index to search in
   * @param query
   *   the [[ElasticQuery]] object representing the search query to execute
   * @return
   *   an instance of [[SearchRequest]] that represents search operation to be performed.
   */
  final def search(index: IndexName, query: ElasticQuery[_]): SearchRequest =
    Search(
      index = index,
      query = query,
      sortBy = Set.empty,
      from = None,
      highlights = None,
      routing = None,
      searchAfter = None,
      size = None
    )

  /**
   * Constructs an instance of [[SearchAndAggregateRequest]] using the specified parameters.
   *
   * @param index
   *   the name of the index to search and aggregate in
   * @param query
   *   an [[ElasticQuery]] object for querying documents
   * @param aggregation
   *   an [[ElasticAggregation]] object for aggregating queried documents
   * @return
   *   an instance of [[SearchAndAggregateRequest]] that represents search and aggregate operations to be performed.
   */
  final def search(
    index: IndexName,
    query: ElasticQuery[_],
    aggregation: ElasticAggregation
  ): SearchAndAggregateRequest =
    SearchAndAggregate(
      index = index,
      query = query,
      aggregation = aggregation,
      sortBy = Set.empty,
      from = None,
      highlights = None,
      routing = None,
      searchAfter = None,
      size = None
    )

  /**
   * Constructs an instance of [[UpdateRequest]] used for updating the document in the specified index, by specified ID.
   *
   * @param index
   *   the name of the index containing the document to update
   * @param id
   *   the ID of the document to update
   * @param doc
   *   the document to be updated, represented by an instance of type `A`
   * @tparam A
   *   the type of the document to be updated. An implicit `Schema` instance must be in scope for this type
   * @return
   *   an instance of [[UpdateRequest]] that represents update operation to be performed.
   */
  final def update[A: Schema](index: IndexName, id: DocumentId, doc: A): UpdateRequest =
    Update(
      index = index,
      id = id,
      doc = Some(Document.from(doc)),
      refresh = None,
      routing = None,
      script = None,
      upsert = None
    )

  /**
   * Constructs an instance of [[UpdateByQueryRequest]] used for updating all documents in the specified index.
   *
   * @param index
   *   the name of the index to update documents in
   * @param script
   *   a [[Script]] object containing the update logic to apply
   * @return
   *   an instance of [[UpdateByQueryRequest]] that represents update all operation to be performed.
   */
  final def updateAllByQuery(index: IndexName, script: Script): UpdateByQueryRequest =
    UpdateByQuery(index = index, script = script, conflicts = None, query = None, refresh = None, routing = None)

  /**
   * Constructs an instance of [[UpdateByQueryRequest]] used for satisfying documents matching specified query in the
   * specified index.
   *
   * @param index
   *   the name of the index to update documents in
   * @param query
   *   an [[ElasticQuery]] object representing a search query used to find documents to update
   * @param script
   *   a [[Script]] object containing the update logic to apply
   * @return
   *   an instance of [[UpdateByQueryRequest]] that represents update by query operation to be performed.
   */
  final def updateByQuery(index: IndexName, query: ElasticQuery[_], script: Script): UpdateByQueryRequest =
    UpdateByQuery(index = index, script = script, conflicts = None, query = Some(query), refresh = None, routing = None)

  /**
   * Constructs an instance of [[UpdateRequest]] used for updating the document with specified ID in the specified
   * index.
   *
   * @param index
   *   the name of the index containing the document to update
   * @param id
   *   the ID of the document to update
   * @param script
   *   a [[Script]] object containing the update logic to apply to the document
   * @return
   *   an instance of [[UpdateRequest]] that represents update by script operation to be performed.
   */
  final def updateByScript(index: IndexName, id: DocumentId, script: Script): UpdateRequest =
    Update(index = index, id = id, doc = None, refresh = None, routing = None, script = Some(script), upsert = None)

  /**
   * Constructs an instance of [[CreateOrUpdateRequest]] used for creating or updating the document in the specified
   * index with specified ID.
   *
   * @param index
   *   the name of the index to create or update the document in
   * @param id
   *   the ID of the document to be created or updated
   * @param doc
   *   the document to be created or updated, represented by an instance of type `A`
   * @tparam A
   *   the type of the document to be created or updated. An implicit `Schema` instance must be in scope for this type
   * @return
   *   an instance of [[CreateOrUpdateRequest]] that represents upsert operation to be performed.
   */
  final def upsert[A: Schema](index: IndexName, id: DocumentId, doc: A): CreateOrUpdateRequest =
    CreateOrUpdate(index = index, id = id, document = Document.from(doc), refresh = None, routing = None)

  sealed trait AggregateRequest extends ElasticRequest[AggregationResult]

  private[elasticsearch] final case class Aggregate(index: IndexName, aggregation: ElasticAggregation)
      extends AggregateRequest

  sealed trait BulkRequest extends ElasticRequest[Unit] with HasRefresh[BulkRequest] with HasRouting[BulkRequest]

  private[elasticsearch] final case class Bulk(
    requests: List[BulkableRequest[_]],
    index: Option[IndexName],
    refresh: Option[Boolean],
    routing: Option[Routing]
  ) extends BulkRequest { self =>
    def refresh(value: Boolean): BulkRequest =
      self.copy(refresh = Some(value))

    def routing(value: Routing): BulkRequest =
      self.copy(routing = Some(value))

    lazy val body: String = requests.flatMap { r =>
      r match {
        case Create(index, document, _, routing) =>
          List(getActionAndMeta("create", List(("_index", Some(index)), ("routing", routing))), document.json)
        case CreateWithId(index, id, document, _, routing) =>
          List(
            getActionAndMeta("create", List(("_index", Some(index)), ("_id", Some(id)), ("routing", routing))),
            document.json
          )
        case CreateOrUpdate(index, id, document, _, routing) =>
          List(
            getActionAndMeta("index", List(("_index", Some(index)), ("_id", Some(id)), ("routing", routing))),
            document.json
          )
        case DeleteById(index, id, _, routing) =>
          List(getActionAndMeta("delete", List(("_index", Some(index)), ("_id", Some(id)), ("routing", routing))))
        case Update(index, id, Some(document), _, routing, None, _) =>
          List(
            getActionAndMeta("update", List(("_index", Some(index)), ("_id", Some(id)), ("routing", routing))),
            Obj("doc" -> document.json)
          )
        case Update(index, id, None, _, routing, Some(script), _) =>
          List(
            getActionAndMeta("update", List(("_index", Some(index)), ("_id", Some(id)), ("routing", routing))),
            Obj("script" -> script.toJson)
          )
      }
    }.mkString(start = "", sep = "\n", end = "\n")
  }

  object Bulk {
    def of(requests: BulkableRequest[_]*): Bulk =
      Bulk(requests = requests.toList, index = None, refresh = None, routing = None)
  }

  sealed trait CountRequest extends ElasticRequest[Int] with HasRouting[CountRequest]

  private[elasticsearch] final case class Count(
    index: IndexName,
    query: Option[ElasticQuery[_]],
    routing: Option[Routing]
  ) extends CountRequest { self =>
    def routing(value: Routing): CountRequest =
      self.copy(routing = Some(value))
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

    def routing(value: Routing): GetByIdRequest =
      self.copy(routing = Some(value))
  }

  sealed trait SearchRequest
      extends ElasticRequest[SearchResult]
      with HasFrom[SearchRequest]
      with HasRouting[SearchRequest]
      with WithSort[SearchRequest]
      with HasSize[SearchRequest] {
    def aggregate(aggregation: ElasticAggregation): SearchAndAggregateRequest

    def highlights(value: Highlights): SearchRequest

    def searchAfter(value: Json): SearchRequest
  }

  private[elasticsearch] final case class Search(
    index: IndexName,
    query: ElasticQuery[_],
    sortBy: Set[Sort],
    from: Option[Int],
    highlights: Option[Highlights],
    routing: Option[Routing],
    searchAfter: Option[Json],
    size: Option[Int]
  ) extends SearchRequest { self =>

    def aggregate(aggregation: ElasticAggregation): SearchAndAggregateRequest =
      SearchAndAggregate(
        index = index,
        query = query,
        aggregation = aggregation,
        sortBy = sortBy,
        from = from,
        highlights = highlights,
        routing = routing,
        searchAfter = None,
        size = size
      )

    def from(value: Int): SearchRequest =
      self.copy(from = Some(value))

    def highlights(value: Highlights): SearchRequest =
      self.copy(highlights = Some(value))

    def routing(value: Routing): SearchRequest =
      self.copy(routing = Some(value))

    def searchAfter(value: Json): SearchRequest =
      self.copy(searchAfter = Some(value))

    def size(value: Int): SearchRequest =
      self.copy(size = Some(value))

    def sort(sorts: Sort*): SearchRequest =
      self.copy(sortBy = sortBy ++ sorts.toSet)

    def toJson: Json = {
      val fromJson: Json = self.from.fold(Obj())(f => Obj("from" -> f.toJson))

      val sizeJson: Json = self.size.fold(Obj())(s => Obj("size" -> s.toJson))

      val highlightsJson: Json = highlights.map(_.toJson).getOrElse(Obj())

      val searchAfterJson: Json = searchAfter.fold(Obj())(sa => Obj("search_after" -> sa))

      val sortJson: Json =
        if (self.sortBy.nonEmpty) Obj("sort" -> Arr(self.sortBy.toList.map(_.paramsToJson): _*)) else Obj()

      fromJson merge sizeJson merge highlightsJson merge sortJson merge self.query.toJson merge searchAfterJson
    }
  }

  sealed trait SearchAndAggregateRequest
      extends ElasticRequest[SearchAndAggregateResult]
      with HasFrom[SearchAndAggregateRequest]
      with HasRouting[SearchAndAggregateRequest]
      with HasSize[SearchAndAggregateRequest]
      with WithSort[SearchAndAggregateRequest] {
    def highlights(value: Highlights): SearchAndAggregateRequest

    def searchAfter(value: Json): SearchAndAggregateRequest
  }

  private[elasticsearch] final case class SearchAndAggregate(
    index: IndexName,
    query: ElasticQuery[_],
    aggregation: ElasticAggregation,
    sortBy: Set[Sort],
    from: Option[Int],
    highlights: Option[Highlights],
    routing: Option[Routing],
    searchAfter: Option[Json],
    size: Option[Int]
  ) extends SearchAndAggregateRequest { self =>
    def from(value: Int): SearchAndAggregateRequest =
      self.copy(from = Some(value))

    def highlights(value: Highlights): SearchAndAggregateRequest =
      self.copy(highlights = Some(value))

    def routing(value: Routing): SearchAndAggregateRequest =
      self.copy(routing = Some(value))

    def size(value: Int): SearchAndAggregateRequest =
      self.copy(size = Some(value))

    def searchAfter(value: Json): SearchAndAggregateRequest =
      self.copy(searchAfter = Some(value))

    def sort(sorts: Sort*): SearchAndAggregateRequest =
      self.copy(sortBy = sortBy ++ sorts.toSet)

    def toJson: Json = {
      val fromJson: Json = self.from.fold(Obj())(f => Obj("from" -> f.toJson))

      val sizeJson: Json = self.size.fold(Obj())(s => Obj("size" -> s.toJson))

      val highlightsJson: Json = highlights.map(_.toJson).getOrElse(Obj())

      val searchAfterJson: Json = searchAfter.fold(Obj())(sa => Obj("search_after" -> sa))

      val sortJson: Json =
        if (self.sortBy.nonEmpty) Obj("sort" -> Arr(self.sortBy.toList.map(_.paramsToJson): _*)) else Obj()

      fromJson merge
        sizeJson merge
        highlightsJson merge
        sortJson merge
        self.query.toJson merge
        aggregation.toJson merge
        searchAfterJson
    }
  }

  sealed trait UpdateRequest
      extends BulkableRequest[UpdateOutcome]
      with HasRefresh[UpdateRequest]
      with HasRouting[UpdateRequest] {
    def orCreate[A: Schema](doc: A): UpdateRequest
  }

  private[elasticsearch] final case class Update(
    index: IndexName,
    id: DocumentId,
    doc: Option[Document],
    refresh: Option[Boolean],
    routing: Option[Routing],
    script: Option[Script],
    upsert: Option[Document]
  ) extends UpdateRequest { self =>
    def orCreate[A: Schema](doc: A): UpdateRequest =
      self.copy(upsert = Some(Document.from(doc)))

    def refresh(value: Boolean): UpdateRequest =
      self.copy(refresh = Some(value))

    def routing(value: Routing): UpdateRequest =
      self.copy(routing = Some(value))

    def toJson: Json = {
      val docToJson: Json = doc.fold(Obj())(d => Obj("doc" -> d.json))

      val scriptToJson: Json = script.fold(Obj())(s => Obj("script" -> s.toJson))

      val upsertJson: Json = upsert.fold(Obj())(u => Obj("upsert" -> u.json))

      scriptToJson merge docToJson merge upsertJson
    }
  }

  sealed trait UpdateByQueryRequest
      extends ElasticRequest[UpdateByQueryResult]
      with HasRefresh[UpdateByQueryRequest]
      with HasRouting[UpdateByQueryRequest] {
    def conflicts(value: UpdateConflicts): UpdateByQueryRequest
  }

  private[elasticsearch] final case class UpdateByQuery(
    index: IndexName,
    script: Script,
    conflicts: Option[UpdateConflicts],
    query: Option[ElasticQuery[_]],
    refresh: Option[Boolean],
    routing: Option[Routing]
  ) extends UpdateByQueryRequest { self =>
    def conflicts(value: UpdateConflicts): UpdateByQueryRequest =
      self.copy(conflicts = Some(value))

    def refresh(value: Boolean): UpdateByQueryRequest =
      self.copy(refresh = Some(value))

    def routing(value: Routing): UpdateByQueryRequest =
      self.copy(routing = Some(value))

    def toJson: Json =
      query.foldLeft(Obj("script" -> script.toJson))((scriptJson, q) => scriptJson merge q.toJson)
  }

  private def getActionAndMeta(requestType: String, parameters: List[(String, Any)]): String =
    parameters.collect { case (name, Some(value)) => s""""$name" : "$value"""" }
      .mkString(s"""{ "$requestType" : { """, ", ", " } }")
}
