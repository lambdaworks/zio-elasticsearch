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

import org.apache.http.HttpHost
import org.elasticsearch.action.ActionListener
import org.elasticsearch.action.get.GetRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.{RequestOptions, RestClient, RestHighLevelClient, RestHighLevelClientBuilder}
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.xcontent.XContentType
import zio.elasticsearch.IndexName
import zio.{Chunk, Task, ZIO}

import scala.annotation.nowarn

@nowarn("cat=deprecation")
private[benchmarks] final class HighLevelRestClient private (client: RestHighLevelClient) extends BenchmarkClient {
  def upsert(index: IndexName, document: TestDocument): Task[Unit] =
    async[org.elasticsearch.action.index.IndexResponse] { listener =>
      val request = new IndexRequest(IndexName.unwrap(index))
        .id(document.id)
        .source(TestDocument.Mapper.writeValueAsString(document), XContentType.JSON)
      client.indexAsync(request, RequestOptions.DEFAULT, listener)
    }.unit

  def getById(index: IndexName, id: String): Task[Option[TestDocument]] =
    async[org.elasticsearch.action.get.GetResponse] { listener =>
      client.getAsync(new GetRequest(IndexName.unwrap(index), id), RequestOptions.DEFAULT, listener)
    }.map(response =>
      if (response.isExists) Some(TestDocument.Mapper.readValue(response.getSourceAsString, classOf[TestDocument]))
      else None
    )

  def search(index: IndexName, term: String): Task[Chunk[TestDocument]] =
    async[org.elasticsearch.action.search.SearchResponse] { listener =>
      val request = new SearchRequest(IndexName.unwrap(index))
        .source(new SearchSourceBuilder().query(QueryBuilders.matchQuery("description", term)))
      client.searchAsync(request, RequestOptions.DEFAULT, listener)
    }.map(response =>
      Chunk
        .fromArray(response.getHits.getHits)
        .map(hit => TestDocument.Mapper.readValue(hit.getSourceAsString, classOf[TestDocument]))
    )

  def tearDown(): Unit = client.close()

  private def async[A](call: ActionListener[A] => Any): Task[A] =
    ZIO.async[Any, Throwable, A] { callback =>
      call(new ActionListener[A] {
        def onResponse(response: A): Unit = callback(ZIO.succeed(response))
        def onFailure(e: Exception): Unit = callback(ZIO.fail(e))
      })
      ()
    }
}

@nowarn("cat=deprecation")
private[benchmarks] object HighLevelRestClient {
  def unsafeMake(): HighLevelRestClient =
    new HighLevelRestClient(
      new RestHighLevelClientBuilder(RestClient.builder(HttpHost.create("http://localhost:9200")).build())
        .setApiCompatibilityMode(true)
        .build()
    )
}
