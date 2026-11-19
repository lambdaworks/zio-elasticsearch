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

package zio.elasticsearch.benchmarks

import sttp.client4.httpclient.zio.SttpClient
import zio._
import zio.elasticsearch.ElasticQuery.matches
import zio.elasticsearch.executor.Executor
import zio.elasticsearch.{DocumentId, ElasticExecutor, ElasticRequest, IndexName}

private[benchmarks] final class ZioElasticsearch private (runtime: Runtime.Scoped[Executor]) extends BenchmarkClient {
  private val executor: Executor = runtime.environment.get[Executor]

  def upsert(index: IndexName, document: TestDocument): Task[Unit] =
    executor.execute(ElasticRequest.upsert(index, DocumentId(document.id), document))

  def getById(index: IndexName, id: String): Task[Option[TestDocument]] =
    executor.execute(ElasticRequest.getById(index, DocumentId(id))).flatMap(_.documentAs[TestDocument])

  def search(index: IndexName, term: String): Task[Chunk[TestDocument]] =
    executor
      .execute(ElasticRequest.search(index, matches("description", term)))
      .flatMap(_.documentAs[TestDocument])

  def prepareIndex(index: IndexName, documents: Chunk[TestDocument]): Task[Unit] =
    for {
      _ <- executor.execute(ElasticRequest.deleteIndex(index))
      _ <- executor.execute(ElasticRequest.createIndex(index))
      _ <- bulkUpsert(index, documents, refresh = true).when(documents.nonEmpty)
    } yield ()

  def bulkUpsert(index: IndexName, documents: Chunk[TestDocument], refresh: Boolean = false): Task[Unit] =
    executor
      .execute(
        ElasticRequest
          .bulk(index, documents.map(doc => ElasticRequest.upsert(DocumentId(doc.id), doc)): _*)
          .refresh(refresh)
      )
      .flatMap { response =>
        if (response.errors) {
          val reason = response.items.iterator.flatMap(_.error).map(_.reason).nextOption().getOrElse("unknown error")
          ZIO.fail(new IllegalStateException(s"Bulk upsert into $index failed: $reason"))
        } else ZIO.unit
      }

  def bulkUpdate(index: IndexName, documents: Chunk[TestDocument]): Task[Unit] =
    executor
      .execute(ElasticRequest.bulk(index, documents.map(doc => ElasticRequest.update(DocumentId(doc.id), doc)): _*))
      .unit

  def tearDown(): Unit =
    Unsafe.unsafe(implicit u => runtime.unsafe.shutdown())
}

private[benchmarks] object ZioElasticsearch {
  def unsafeMake(backend: TaskLayer[SttpClient]): ZioElasticsearch =
    new ZioElasticsearch(Unsafe.unsafe(implicit u => Runtime.unsafe.fromLayer(backend >>> ElasticExecutor.local)))
}
