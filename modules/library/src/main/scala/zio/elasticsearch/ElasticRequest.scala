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
import zio.elasticsearch.result.{AggregationResult, GetResult, SearchAndAggregateResult, SearchResult}
import zio.elasticsearch.script.Script
import zio.json.ast.Json
import zio.json.ast.Json.{Arr, Obj}
import zio.schema.Schema

sealed trait BulkableRequest[A] extends ElasticRequest[A]

sealed trait ElasticRequest[A]

object ElasticRequest {

  def aggregate(index: IndexName, aggregation: ElasticAggregation): AggregateRequest =
    Aggregate(index = index, aggregation = aggregation)

  def bulk(requests: BulkableRequest[_]*): BulkRequest =
    Bulk.of(requests = requests: _*)

  def count(index: IndexName): CountRequest =
    Count(index = index, query = None, routing = None)

  def count(index: IndexName, query: ElasticQuery[_]): CountRequest =
    Count(index = index, query = Some(query), routing = None)

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

  def search(index: IndexName, query: ElasticQuery[_], aggregation: ElasticAggregation): SearchAndAggregateRequest =
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

  def update[A: Schema](index: IndexName, id: DocumentId, doc: A): UpdateRequest =
    Update(
      index = index,
      id = id,
      doc = Some(Document.from(doc)),
      docAsUpsert = None,
      refresh = None,
      routing = None,
      script = None,
      upsert = None
    )

  def updateByScript(index: IndexName, id: DocumentId, script: Script): UpdateRequest =
    Update(
      index = index,
      id = id,
      doc = None,
      docAsUpsert = None,
      refresh = None,
      routing = None,
      script = Some(script),
      upsert = None
    )

  def upsert[A: Schema](index: IndexName, id: DocumentId, doc: A): CreateOrUpdateRequest =
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
        case Update(index, id, _, _, _, maybeRouting, _, _) =>
          List(getActionAndMeta("update", List(("_index", Some(index)), ("_id", Some(id)), ("routing", maybeRouting))))
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

    def sortBy(sorts: Sort*): SearchRequest =
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

    def sortBy(sorts: Sort*): SearchAndAggregateRequest =
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

    def docAsUpsert(value: Boolean): UpdateRequest

    def docAsUpsertFalse: UpdateRequest = docAsUpsert(value = false)

    def docAsUpsertTrue: UpdateRequest = docAsUpsert(value = true)

    def orCreate[A: Schema](doc: A): UpdateRequest
  }

  private[elasticsearch] final case class Update(
    index: IndexName,
    id: DocumentId,
    doc: Option[Document],
    docAsUpsert: Option[Boolean],
    refresh: Option[Boolean],
    routing: Option[Routing],
    script: Option[Script],
    upsert: Option[Document]
  ) extends UpdateRequest { self =>
    def docAsUpsert(value: Boolean): UpdateRequest =
      self.copy(docAsUpsert = Some(value))

    def orCreate[A: Schema](doc: A): UpdateRequest =
      self.copy(upsert = Some(Document.from(doc)))

    def refresh(value: Boolean): UpdateRequest =
      self.copy(refresh = Some(value))

    def refreshFalse: UpdateRequest =
      refresh(value = false)

    def refreshTrue: UpdateRequest =
      refresh(value = true)

    def routing(value: Routing): UpdateRequest =
      self.copy(routing = Some(value))

    def toJson: Json = {
      val docToJson: Json = doc.fold(Obj())(d => Obj("doc" -> d.json))

      val docAsUpsertJson: Json = docAsUpsert.fold(Obj())(d => Obj("doc_as_upsert" -> d.toJson))

      val scriptToJson: Json = script.fold(Obj())(s => Obj("script" -> s.toJson))

      val upsertJson: Json = upsert.fold(Obj())(u => Obj("upsert" -> u.json))

      scriptToJson merge docToJson merge docAsUpsertJson merge upsertJson
    }
  }

  private def getActionAndMeta(requestType: String, parameters: List[(String, Any)]): String =
    parameters.collect { case (name, Some(value)) => s""""$name" : "$value"""" }
      .mkString(s"""{ "$requestType" : { """, ", ", " } }")
}
