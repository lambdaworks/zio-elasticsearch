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

trait HasRefresh[A] { // TODO extend this on all appropritate requests
  def refresh(value: Boolean): ElasticRequest[A]

  def refreshFalse: ElasticRequest[A]

  def refreshTrue: ElasticRequest[A]
}

trait HasRouting[A] { // TODO extend this on all appropritate requests
  def routing(value: Routing): ElasticRequest[A]
}

sealed trait ElasticRequest[A]

sealed trait BulkableRequest[A] extends ElasticRequest[A]

object ElasticRequest {

  def bulk(requests: BulkableRequest[_]*): Bulk =
    Bulk.of(requests: _*)

  def create[A: Schema](index: IndexName, doc: A): Create =
    Create(index = index, document = Document.from(doc), refresh = false, routing = None)

  def create[A: Schema](index: IndexName, id: DocumentId, doc: A): CreateWithId =
    CreateWithId(index = index, id = id, document = Document.from(doc), refresh = false, routing = None)

  def createIndex(name: IndexName): CreateIndex =
    CreateIndex(name = name, definition = None)

  def createIndex(name: IndexName, definition: String): CreateIndex =
    CreateIndex(name = name, definition = Some(definition))

  def deleteById(index: IndexName, id: DocumentId): DeleteById =
    DeleteById(index = index, id = id, refresh = false, routing = None)

  def deleteByQuery(index: IndexName, query: ElasticQuery[_]): DeleteByQuery =
    DeleteByQuery(index = index, query = query, refresh = false, routing = None)

  def deleteIndex(name: IndexName): DeleteIndex =
    DeleteIndex(name)

  def exists(index: IndexName, id: DocumentId): Exists =
    Exists(index = index, id = id, routing = None)

  def getById(index: IndexName, id: DocumentId): GetById =
    GetById(index = index, id = id, routing = None)

  def search(index: IndexName, query: ElasticQuery[_]): GetByQuery =
    GetByQuery(index = index, query = query, routing = None)

  def upsert[A: Schema](index: IndexName, id: DocumentId, doc: A): CreateOrUpdate =
    CreateOrUpdate(index = index, id = id, document = Document.from(doc), refresh = false, routing = None)

  sealed trait BulkRequest extends ElasticRequest[Unit] with HasRouting[Unit] with HasRefresh[Unit]

  private[elasticsearch] final case class Bulk(
    requests: List[BulkableRequest[_]],
    index: Option[IndexName],
    refresh: Boolean,
    routing: Option[Routing]
  ) extends BulkRequest { self =>
    def routing(value: Routing): Bulk = self.copy(routing = Some(value))

    def refresh(value: Boolean): Bulk = self.copy(refresh = value)

    def refreshFalse: Bulk = refresh(false)

    def refreshTrue: Bulk = refresh(true)

    lazy val body: String = requests.flatMap { r =>
      // We use @unchecked to ignore 'pattern match not exhaustive' error since we guarantee that it will not happen
      // because these are only Bulkable Requests and other matches will not occur.
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
      Bulk(requests = requests.toList, index = None, refresh = false, routing = None)
  }

  sealed trait CreateRequest extends BulkableRequest[DocumentId] with HasRouting[DocumentId] with HasRefresh[DocumentId]

  private[elasticsearch] final case class Create(
    index: IndexName,
    document: Document,
    refresh: Boolean,
    routing: Option[Routing]
  ) extends CreateRequest { self =>
    def routing(value: Routing): Create = self.copy(routing = Some(value))

    def refresh(value: Boolean): Create = self.copy(refresh = value)

    def refreshFalse: Create = refresh(false)

    def refreshTrue: Create = refresh(true)
  }

  sealed trait CreateWithIdRequest
      extends BulkableRequest[CreationOutcome]
      with HasRouting[CreationOutcome]
      with HasRefresh[CreationOutcome]

  private[elasticsearch] final case class CreateWithId(
    index: IndexName,
    id: DocumentId,
    document: Document,
    refresh: Boolean,
    routing: Option[Routing]
  ) extends CreateWithIdRequest { self =>
    def routing(value: Routing): CreateWithId = self.copy(routing = Some(value))

    def refresh(value: Boolean): CreateWithId = self.copy(refresh = value)

    def refreshFalse: CreateWithId = refresh(false)

    def refreshTrue: CreateWithId = refresh(true)
  }

  sealed trait CreateIndexRequest extends ElasticRequest[CreationOutcome]

  private[elasticsearch] final case class CreateIndex(
    name: IndexName,
    definition: Option[String]
  ) extends ElasticRequest[CreationOutcome]

  sealed trait CreateOrUpdateRequest extends BulkableRequest[Unit] with HasRouting[Unit] with HasRefresh[Unit]

  private[elasticsearch] final case class CreateOrUpdate(
    index: IndexName,
    id: DocumentId,
    document: Document,
    refresh: Boolean,
    routing: Option[Routing]
  ) extends CreateOrUpdateRequest { self =>
    def routing(value: Routing): CreateOrUpdate = self.copy(routing = Some(value))

    def refresh(value: Boolean): CreateOrUpdate = self.copy(refresh = value)

    def refreshFalse: CreateOrUpdate = refresh(false)

    def refreshTrue: CreateOrUpdate = refresh(true)
  }

  sealed trait DeleteByIdRequest
      extends BulkableRequest[DeletionOutcome]
      with HasRouting[DeletionOutcome]
      with HasRefresh[DeletionOutcome]

  private[elasticsearch] final case class DeleteById(
    index: IndexName,
    id: DocumentId,
    refresh: Boolean,
    routing: Option[Routing]
  ) extends DeleteByIdRequest { self =>
    def routing(value: Routing): DeleteById = self.copy(routing = Some(value))

    def refresh(value: Boolean): DeleteById = self.copy(refresh = value)

    def refreshFalse: DeleteById = refresh(false)

    def refreshTrue: DeleteById = refresh(true)
  }

  sealed trait DeleteByQueryRequest
      extends ElasticRequest[DeletionOutcome]
      with HasRouting[DeletionOutcome]
      with HasRefresh[DeletionOutcome]

  private[elasticsearch] final case class DeleteByQuery(
    index: IndexName,
    query: ElasticQuery[_],
    refresh: Boolean,
    routing: Option[Routing]
  ) extends DeleteByQueryRequest { self =>
    def routing(value: Routing): DeleteByQuery = self.copy(routing = Some(value))

    def refresh(value: Boolean): DeleteByQuery = self.copy(refresh = value)

    def refreshFalse: DeleteByQuery = refresh(false)

    def refreshTrue: DeleteByQuery = refresh(true)
  }

  sealed trait DeleteIndexRequest extends ElasticRequest[DeletionOutcome]

  final case class DeleteIndex(name: IndexName) extends DeleteIndexRequest

  sealed trait ExistRequest extends ElasticRequest[Boolean] with HasRouting[Boolean]

  private[elasticsearch] final case class Exists(
    index: IndexName,
    id: DocumentId,
    routing: Option[Routing]
  ) extends ExistRequest { self =>
    def routing(value: Routing): Exists = self.copy(routing = Some(value))
  }

  sealed trait GetByIdRequest extends ElasticRequest[GetResult] with HasRouting[GetResult]

  private[elasticsearch] final case class GetById(
    index: IndexName,
    id: DocumentId,
    routing: Option[Routing]
  ) extends GetByIdRequest { self =>
    def routing(value: Routing): GetById = self.copy(routing = Some(value))
  }

  sealed trait GetByQueryRequest extends ElasticRequest[SearchResult]

  private[elasticsearch] final case class GetByQuery(
    index: IndexName,
    query: ElasticQuery[_],
    routing: Option[Routing]
  ) extends GetByQueryRequest

  private def getActionAndMeta(requestType: String, parameters: List[(String, Any)]): String =
    parameters.collect { case (name, Some(value)) => s""""$name" : "${value.toString}"""" }
      .mkString(s"""{ "$requestType" : { """, ", ", " } }")

}

sealed abstract class CreationOutcome

case object Created       extends CreationOutcome
case object AlreadyExists extends CreationOutcome

sealed abstract class DeletionOutcome

case object Deleted  extends DeletionOutcome
case object NotFound extends DeletionOutcome
