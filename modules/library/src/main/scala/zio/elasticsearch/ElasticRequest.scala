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

  def bulk(requests: BulkableRequest[_]*): BulkRequest =
    BulkRequest.of(requests: _*)

  def create[A: Schema](index: IndexName, doc: A): CreateRequest =
    CreateRequest(index = index, document = Document.from(doc), refresh = false, routing = None)

  def create[A: Schema](index: IndexName, id: DocumentId, doc: A): CreateWithIdRequest =
    CreateWithIdRequest(index = index, id = id, document = Document.from(doc), refresh = false, routing = None)

  def createIndex(name: IndexName): ElasticRequest[CreationOutcome, CreateIndex] =
    CreateIndexRequest(name = name, definition = None)

  def createIndex(name: IndexName, definition: String): ElasticRequest[CreationOutcome, CreateIndex] =
    CreateIndexRequest(name = name, definition = Some(definition))

  def deleteById(index: IndexName, id: DocumentId): DeleteByIdRequest =
    DeleteByIdRequest(index = index, id = id, refresh = false, routing = None)

  def deleteByQuery(index: IndexName, query: ElasticQuery[_]): DeleteByQueryRequest =
    DeleteByQueryRequest(index = index, query = query, refresh = false, routing = None)

  def deleteIndex(name: IndexName): DeleteIndexRequest =
    DeleteIndexRequest(name)

  def exists(index: IndexName, id: DocumentId): ExistsRequest =
    ExistsRequest(index = index, id = id, routing = None)

  def getById(index: IndexName, id: DocumentId): GetByIdRequest =
    GetByIdRequest(index = index, id = id, routing = None)

  def search(index: IndexName, query: ElasticQuery[_]): GetByQueryRequest =
    GetByQueryRequest(index = index, query = query, routing = None)

  def upsert[A: Schema](index: IndexName, id: DocumentId, doc: A): ElasticRequest[Unit, Upsert] =
    CreateOrUpdateRequest(index = index, id = id, document = Document.from(doc), refresh = false, routing = None)

  final case class BulkRequest(
    requests: List[BulkableRequest[_]],
    index: Option[IndexName],
    refresh: Boolean,
    routing: Option[Routing]
  ) extends ElasticRequest[Unit]
      with HasRouting[Unit]
      with HasRefresh[Unit] { self =>
    override def routing(value: Routing) = self.copy(routing = Some(value))

    override def refresh(value: Boolean) = self.copy(refresh = value)

    override def refreshFalse = refresh(false)

    override def refreshTrue = refresh(true)

    lazy val body: String = requests.flatMap { r =>
      // We use @unchecked to ignore 'pattern match not exhaustive' error since we guarantee that it will not happen
      // because these are only Bulkable Requests and other matches will not occur.
      r match {
        case CreateRequest(index, document, _, maybeRouting) =>
          List(getActionAndMeta("create", List(("_index", Some(index)), ("routing", maybeRouting))), document.json)
        case CreateWithIdRequest(index, id, document, _, maybeRouting) =>
          List(
            getActionAndMeta("create", List(("_index", Some(index)), ("_id", Some(id)), ("routing", maybeRouting))),
            document.json
          )
        case CreateOrUpdateRequest(index, id, document, _, maybeRouting) =>
          List(
            getActionAndMeta("index", List(("_index", Some(index)), ("_id", Some(id)), ("routing", maybeRouting))),
            document.json
          )
        case DeleteByIdRequest(index, id, _, maybeRouting) =>
          List(getActionAndMeta("delete", List(("_index", Some(index)), ("_id", Some(id)), ("routing", maybeRouting))))
      }
    }.mkString(start = "", sep = "\n", end = "\n")
  }

  object BulkRequest {
    def of(requests: BulkableRequest[_]*): BulkRequest =
      BulkRequest(requests = requests.toList, index = None, refresh = false, routing = None)
  }

  final case class CreateRequest(
    index: IndexName,
    document: Document,
    refresh: Boolean,
    routing: Option[Routing]
  ) extends BulkableRequest[DocumentId]
      with HasRouting[DocumentId]
      with HasRefresh[DocumentId] { self =>
    override def routing(value: Routing) = self.copy(routing = Some(value))

    override def refresh(value: Boolean) = self.copy(refresh = value)

    override def refreshFalse = refresh(false)

    override def refreshTrue = refresh(true)
  }

  final case class CreateWithIdRequest(
    index: IndexName,
    id: DocumentId,
    document: Document,
    refresh: Boolean,
    routing: Option[Routing]
  ) extends BulkableRequest[CreationOutcome]
      with HasRouting[CreationOutcome]
      with HasRefresh[CreationOutcome] { self =>
    override def routing(value: Routing) = self.copy(routing = Some(value))

    override def refresh(value: Boolean) = self.copy(refresh = value)

    override def refreshFalse = refresh(false)

    override def refreshTrue = refresh(true)
  }

  final case class CreateIndexRequest(
    name: IndexName,
    definition: Option[String]
  ) extends ElasticRequest[CreationOutcome]

  final case class CreateOrUpdateRequest(
    index: IndexName,
    id: DocumentId,
    document: Document,
    refresh: Boolean,
    routing: Option[Routing]
  ) extends BulkableRequest[Unit]
      with HasRouting[Unit]
      with HasRefresh[Unit] { self =>
    override def routing(value: Routing) = self.copy(routing = Some(value))

    override def refresh(value: Boolean) = self.copy(refresh = value)

    override def refreshFalse = refresh(false)

    override def refreshTrue = refresh(true)
  }

  final case class DeleteByIdRequest(
    index: IndexName,
    id: DocumentId,
    refresh: Boolean,
    routing: Option[Routing]
  ) extends BulkableRequest[DeletionOutcome]
      with HasRouting[DeletionOutcome]
      with HasRefresh[DeletionOutcome] { self =>
    override def routing(value: Routing) = self.copy(routing = Some(value))

    override def refresh(value: Boolean) = self.copy(refresh = value)

    override def refreshFalse = refresh(false)

    override def refreshTrue = refresh(true)
  }

  final case class DeleteByQueryRequest(
    index: IndexName,
    query: ElasticQuery[_],
    refresh: Boolean,
    routing: Option[Routing]
  ) extends ElasticRequest[DeletionOutcome]
      with HasRouting[DeletionOutcome]
      with HasRefresh[DeletionOutcome] { self =>
    override def routing(value: Routing) = self.copy(routing = Some(value))

    override def refresh(value: Boolean) = self.copy(refresh = value)

    override def refreshFalse = refresh(false)

    override def refreshTrue = refresh(true)
  }

  final case class DeleteIndexRequest(name: IndexName) extends ElasticRequest[DeletionOutcome]

  final case class ExistsRequest(
    index: IndexName,
    id: DocumentId,
    routing: Option[Routing]
  ) extends ElasticRequest[Boolean]
      with HasRouting[Boolean] { self =>
    override def routing(value: Routing) = self.copy(routing = Some(value))
  }

  final case class GetByIdRequest(
    index: IndexName,
    id: DocumentId,
    routing: Option[Routing]
  ) extends ElasticRequest[GetResult]
      with HasRouting[GetResult] { self =>
    override def routing(value: Routing) = self.copy(routing = Some(value))
  }

  final case class GetByQueryRequest(
    index: IndexName,
    query: ElasticQuery[_],
    routing: Option[Routing]
  ) extends ElasticRequest[SearchResult]

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
