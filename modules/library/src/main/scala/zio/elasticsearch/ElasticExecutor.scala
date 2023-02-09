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

import sttp.client3.SttpBackend
import zio.elasticsearch.ElasticRequest.BulkableRequest
import zio.elasticsearch.ElasticRequestType._
import zio.schema.Schema
import zio.stm.TMap
import zio.{Task, ULayer, ZLayer}

trait ElasticExecutor {

  def bulk(requests: BulkableRequest*): ElasticRequest[Unit, Bulk]

  def create[A: Schema](index: IndexName, doc: A): ElasticRequest[DocumentId, Create]

  def create[A: Schema](index: IndexName, id: DocumentId, doc: A): ElasticRequest[CreationOutcome, CreateWithId]

  def createIndex(name: IndexName, definition: Option[String]): ElasticRequest[CreationOutcome, CreateIndex]

  def deleteById(index: IndexName, id: DocumentId): ElasticRequest[DeletionOutcome, DeleteById]

  def deleteByQuery(index: IndexName, query: ElasticQuery[_]): ElasticRequest[DeletionOutcome, DeleteByQuery]

  def deleteIndex(name: IndexName): ElasticRequest[DeletionOutcome, DeleteIndex]

  def exists(index: IndexName, id: DocumentId): ElasticRequest[Boolean, Exists]

  def getById[A: Schema](index: IndexName, id: DocumentId): ElasticRequest[Option[A], GetById]

  def search[A](index: IndexName, query: ElasticQuery[_])(implicit schema: Schema[A]): ElasticRequest[List[A], GetByQuery]

  def upsert[A: Schema](index: IndexName, id: DocumentId, doc: A): ElasticRequest[Unit, Upsert]
}

object ElasticExecutor {
  lazy val live: ZLayer[ElasticConfig with SttpBackend[Task, Any], Throwable, ElasticExecutor] =
    ZLayer.fromFunction(HttpElasticExecutor.apply _)

  lazy val local: ZLayer[SttpBackend[Task, Any], Throwable, ElasticExecutor] =
    ZLayer.succeed(ElasticConfig.Default) >>> live

  lazy val test: ULayer[TestExecutor] =
    ZLayer(TMap.empty[IndexName, TMap[DocumentId, Document]].map(TestExecutor).commit)
}
