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

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.http.JavaClient
import com.sksamuel.elastic4s.jackson.ElasticJackson.Implicits._
import com.sksamuel.elastic4s.zio.instances._
import com.sksamuel.elastic4s.{ElasticClient, ElasticProperties}
import zio.elasticsearch.IndexName
import zio.{Chunk, Task}

private[benchmarks] final class Elastic4s private (client: ElasticClient) extends BenchmarkClient {
  def upsert(index: IndexName, document: TestDocument): Task[Unit] =
    client.execute(indexInto(IndexName.unwrap(index)).id(document.id).doc(document)).map(_.result).unit

  def getById(index: IndexName, id: String): Task[Option[TestDocument]] =
    client.execute(get(IndexName.unwrap(index), id)).map(_.result.toOpt[TestDocument])

  def search(index: IndexName, term: String): Task[Chunk[TestDocument]] =
    client
      .execute(com.sksamuel.elastic4s.ElasticDsl.search(IndexName.unwrap(index)).query(matchQuery("description", term)))
      .map(response => Chunk.fromArray(response.result.to[TestDocument].toArray))

  def tearDown(): Unit = client.close()
}

private[benchmarks] object Elastic4s {
  def unsafeMake(): Elastic4s =
    new Elastic4s(ElasticClient(JavaClient(ElasticProperties("http://localhost:9200"))))
}
