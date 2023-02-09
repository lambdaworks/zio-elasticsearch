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
import zio.elasticsearch.ElasticRequestType.{Bulk, Create, CreateWithId, DeleteById, Exists, GetById, Upsert}
import zio.prelude.Assertion.isEmptyString
import zio.prelude.Newtype

object Routing extends Newtype[String] {
  override def assertion = assert(!isEmptyString) // scalafix:ok

  type Routing = Routing.Type

  trait WithRouting[ERT <: ElasticRequestType] {
    def withRouting[A](request: ElasticRequest[A, ERT], routing: Routing): ElasticRequest[A, ERT]
  }

  object WithRouting {
    implicit val bulkWithRouting: WithRouting[Bulk] = new WithRouting[Bulk] {
      def withRouting[A](request: ElasticRequest[A, Bulk], routing: Routing): ElasticRequest[A, Bulk] =
        request match {
          case Map(r, mapper) => Map(withRouting(r, routing), mapper)
          case r: BulkRequest => new BulkRequest(r.requests, r.index, r.refresh, Some(routing)) {
            def execute: Task[Unit] = r.execute
          }
        }
    }

    implicit val createWithRouting: WithRouting[Create] = new WithRouting[Create] {
      def withRouting[A](request: ElasticRequest[A, Create], routing: Routing): ElasticRequest[A, Create] =
        request match {
          case Map(r, mapper)   => Map(withRouting(r, routing), mapper)
          case r: CreateRequest => new CreateRequest(r.index, r.document, r.refresh, Some(routing)) {
            def execute: Task[DocumentId] = r.execute
          }
        }
    }

    implicit val createWithIdWithRouting: WithRouting[CreateWithId] = new WithRouting[CreateWithId] {
      def withRouting[A](request: ElasticRequest[A, CreateWithId], routing: Routing): ElasticRequest[A, CreateWithId] =
        request match {
          case Map(r, mapper)         => Map(withRouting(r, routing), mapper)
          case r: CreateWithIdRequest => new CreateWithIdRequest(r.index, r.id, r.document, r.refresh, Some(routing)) {
            def execute: Task[CreationOutcome] = r.execute
          }
        }
    }

    implicit val deleteByIdWithRouting: WithRouting[DeleteById] = new WithRouting[DeleteById] {
      def withRouting[A](request: ElasticRequest[A, DeleteById], routing: Routing): ElasticRequest[A, DeleteById] =
        request match {
          case Map(r, mapper)       => Map(withRouting(r, routing), mapper)
          case r: DeleteByIdRequest => new DeleteByIdRequest(r.index, r.id, r.refresh, Some(routing)) {
            def execute: Task[DeletionOutcome] = r.execute
          }
        }
    }

    implicit val existsWithRouting: WithRouting[Exists] = new WithRouting[Exists] {
      def withRouting[A](request: ElasticRequest[A, Exists], routing: Routing): ElasticRequest[A, Exists] =
        request match {
          case Map(r, mapper)   => Map(withRouting(r, routing), mapper)
          case r: ExistsRequest => new ExistsRequest(r.index, r.id, Some(routing)) {
            def execute: Task[Boolean] = r.execute
          }
        }
    }

    implicit val getByIdWithRouting: WithRouting[GetById] = new WithRouting[GetById] {
      def withRouting[A](request: ElasticRequest[A, GetById], routing: Routing): ElasticRequest[A, GetById] =
        request match {
          case Map(r, mapper)    => Map(withRouting(r, routing), mapper)
          case r: GetByIdRequest => new GetByIdRequest(r.index, r.id, Some(routing)) {
            def execute: Task[Option[Document]] = r.execute
          }
        }
    }

    implicit val upsertWithRouting: WithRouting[Upsert] = new WithRouting[Upsert] {
      def withRouting[A](request: ElasticRequest[A, Upsert], routing: Routing): ElasticRequest[A, Upsert] =
        request match {
          case Map(r, mapper)           => Map(withRouting(r, routing), mapper)
          case r: CreateOrUpdateRequest => new CreateOrUpdateRequest(r.index, r.id, r.document, r.refresh, Some(routing)) {
            def execute: Task[Unit] = r.execute
          }
        }
    }
  }
}
