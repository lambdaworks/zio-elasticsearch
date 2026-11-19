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

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient
import co.elastic.clients.elasticsearch.core.{GetRequest, IndexRequest, SearchRequest}
import co.elastic.clients.json.jackson.JacksonJsonpMapper
import co.elastic.clients.transport.rest_client.RestClientTransport
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import zio.elasticsearch.IndexName
import zio.{Chunk, Task, ZIO}

import scala.jdk.CollectionConverters._

private[benchmarks] final class ElasticsearchJava private (transport: RestClientTransport) extends BenchmarkClient {
  private val client: ElasticsearchAsyncClient = new ElasticsearchAsyncClient(transport)

  def upsert(index: IndexName, document: TestDocument): Task[Unit] =
    ZIO.fromCompletableFuture {
      client.index(
        IndexRequest.of[TestDocument]((b: IndexRequest.Builder[TestDocument]) =>
          b.index(IndexName.unwrap(index)).id(document.id).document(document)
        )
      )
    }.unit

  def getById(index: IndexName, id: String): Task[Option[TestDocument]] =
    ZIO.fromCompletableFuture {
      client.get(
        GetRequest.of((b: GetRequest.Builder) => b.index(IndexName.unwrap(index)).id(id)),
        classOf[TestDocument]
      )
    }.map(response => Option(response.source()))

  def search(index: IndexName, term: String): Task[Chunk[TestDocument]] =
    ZIO.fromCompletableFuture {
      client.search(
        SearchRequest.of((b: SearchRequest.Builder) =>
          b.index(IndexName.unwrap(index)).query(q => q.`match`(m => m.field("description").query(term)))
        ),
        classOf[TestDocument]
      )
    }.map(response => Chunk.fromIterable(response.hits().hits().asScala.map(_.source())))

  def tearDown(): Unit = transport.close()
}

private[benchmarks] object ElasticsearchJava {
  def unsafeMake(): ElasticsearchJava =
    new ElasticsearchJava(
      new RestClientTransport(
        RestClient.builder(HttpHost.create("http://localhost:9200")).build(),
        new JacksonJsonpMapper(TestDocument.Mapper)
      )
    )
}
