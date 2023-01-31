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

import zio.elasticsearch.ElasticRequest._
import zio.elasticsearch.ElasticRequestType._

object Refresh {

  trait WithRefresh[ERT <: ElasticRequestType] {
    def withRefresh[A](request: ElasticRequest[A, ERT], value: Boolean): ElasticRequest[A, ERT]
  }

  object WithRefresh {
    implicit val bulkWithRefresh: WithRefresh[Bulk] = new WithRefresh[Bulk] {
      def withRefresh[A](request: ElasticRequest[A, Bulk], value: Boolean): ElasticRequest[A, Bulk] =
        request match {
          case Map(r, mapper) => Map(withRefresh(r, value), mapper)
          case r: BulkRequest => r.copy(refresh = value)
        }
    }

    implicit val createWithRefresh: WithRefresh[Create] = new WithRefresh[Create] {
      def withRefresh[A](request: ElasticRequest[A, Create], value: Boolean): ElasticRequest[A, Create] =
        request match {
          case Map(r, mapper)   => Map(withRefresh(r, value), mapper)
          case r: CreateRequest => r.copy(refresh = value)
        }
    }

    implicit val createWithIdWithRefresh: WithRefresh[CreateWithId] = new WithRefresh[CreateWithId] {
      def withRefresh[A](request: ElasticRequest[A, CreateWithId], value: Boolean): ElasticRequest[A, CreateWithId] =
        request match {
          case Map(r, mapper)         => Map(withRefresh(r, value), mapper)
          case r: CreateWithIdRequest => r.copy(refresh = value)
        }
    }

    implicit val deleteByIdWithRefresh: WithRefresh[DeleteById] = new WithRefresh[DeleteById] {
      def withRefresh[A](request: ElasticRequest[A, DeleteById], value: Boolean): ElasticRequest[A, DeleteById] =
        request match {
          case Map(r, mapper)       => Map(withRefresh(r, value), mapper)
          case r: DeleteByIdRequest => r.copy(refresh = value)
        }
    }

    implicit val deleteByQueryWithRefresh: WithRefresh[DeleteByQuery] = new WithRefresh[DeleteByQuery] {
      def withRefresh[A](request: ElasticRequest[A, DeleteByQuery], value: Boolean): ElasticRequest[A, DeleteByQuery] =
        request match {
          case Map(r, mapper)          => Map(withRefresh(r, value), mapper)
          case r: DeleteByQueryRequest => r.copy(refresh = value)
        }
    }

    implicit val upsertWithRefresh: WithRefresh[Upsert] = new WithRefresh[Upsert] {
      def withRefresh[A](request: ElasticRequest[A, Upsert], value: Boolean): ElasticRequest[A, Upsert] =
        request match {
          case Map(r, mapper)           => Map(withRefresh(r, value), mapper)
          case r: CreateOrUpdateRequest => r.copy(refresh = value)
        }
    }
  }
}
