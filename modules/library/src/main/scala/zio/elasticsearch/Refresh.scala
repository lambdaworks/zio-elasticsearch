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

import zio.Task
import zio.elasticsearch.ElasticRequest._
import zio.elasticsearch.ElasticRequestType._
import zio.elasticsearch.Routing.Routing

object Refresh {

  trait WithRefresh[ERT <: ElasticRequestType] {
    def withRefresh[A](request: ElasticRequest[A, ERT], value: Boolean): ElasticRequest[A, ERT]
  }

  object WithRefresh {
    implicit val bulkWithRefresh: WithRefresh[Bulk] = new WithRefresh[Bulk] {
      def withRefresh[A](request: ElasticRequest[A, Bulk], value: Boolean): ElasticRequest[A, Bulk] =
        request match {
          case Map(r, mapper) => Map(withRefresh(r, value), mapper)
          case r: BulkRequest =>
            new BulkRequest(r.requests, r.index, value, r.routing) {
              def execute(requests: List[BulkableRequest], index: Option[IndexName], refresh: Boolean, routing: Option[Routing]): Task[Unit] =
                r.execute(requests, index, refresh, routing)
            }
        }
    }

    implicit val createWithRefresh: WithRefresh[Create] = new WithRefresh[Create] {
      def withRefresh[A](request: ElasticRequest[A, Create], value: Boolean): ElasticRequest[A, Create] =
        request match {
          case Map(r, mapper) => Map(withRefresh(r, value), mapper)
          case r: CreateRequest =>
            new CreateRequest(r.index, r.document, value, r.routing) {
              def execute(index: IndexName, document: Document, refresh: Boolean, routing: Option[Routing]): Task[DocumentId] =
                r.execute(index, document, refresh, routing)
            }
        }
    }

    implicit val createWithIdWithRefresh: WithRefresh[CreateWithId] = new WithRefresh[CreateWithId] {
      def withRefresh[A](request: ElasticRequest[A, CreateWithId], value: Boolean): ElasticRequest[A, CreateWithId] =
        request match {
          case Map(r, mapper) => Map(withRefresh(r, value), mapper)
          case r: CreateWithIdRequest =>
            new CreateWithIdRequest(r.index, r.id, r.document, value, r.routing) {
              def execute(index: IndexName, id: DocumentId, document: Document, refresh: Boolean, routing: Option[Routing]): Task[CreationOutcome] =
                r.execute(index, id, document, refresh, routing)
            }
        }
    }

    implicit val deleteByIdWithRefresh: WithRefresh[DeleteById] = new WithRefresh[DeleteById] {
      def withRefresh[A](request: ElasticRequest[A, DeleteById], value: Boolean): ElasticRequest[A, DeleteById] =
        request match {
          case Map(r, mapper) => Map(withRefresh(r, value), mapper)
          case r: DeleteByIdRequest =>
            new DeleteByIdRequest(r.index, r.id, value, r.routing) {
              def execute(index: IndexName, id: DocumentId, refresh: Boolean, routing: Option[Routing]): Task[DeletionOutcome] =
                r.execute(index, id, refresh, routing)
            }
        }
    }

    implicit val deleteByQueryWithRefresh: WithRefresh[DeleteByQuery] = new WithRefresh[DeleteByQuery] {
      def withRefresh[A](request: ElasticRequest[A, DeleteByQuery], value: Boolean): ElasticRequest[A, DeleteByQuery] =
        request match {
          case Map(r, mapper) => Map(withRefresh(r, value), mapper)
          case r: DeleteByQueryRequest =>
            new DeleteByQueryRequest(r.index, r.query, value, r.routing) {
              def execute(index: IndexName, query: ElasticQuery[_], refresh: Boolean, routing: Option[Routing]): Task[DeletionOutcome] =
                r.execute(index, query, refresh, routing)
            }
        }
    }

    implicit val upsertWithRefresh: WithRefresh[Upsert] = new WithRefresh[Upsert] {
      def withRefresh[A](request: ElasticRequest[A, Upsert], value: Boolean): ElasticRequest[A, Upsert] =
        request match {
          case Map(r, mapper) => Map(withRefresh(r, value), mapper)
          case r: CreateOrUpdateRequest =>
            new CreateOrUpdateRequest(r.index, r.id, r.document, value, r.routing) {
              def execute(index: IndexName, id: DocumentId, document: Document, refresh: Boolean, routing: Option[Routing]): Task[Unit] =
                r.execute(index, id, document, refresh, routing)
            }
        }
    }
  }
}
