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

import org.openjdk.jmh.annotations._
import zio.Chunk
import zio.elasticsearch.IndexName

import java.util.concurrent.TimeUnit

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Measurement(iterations = 5, time = 1)
@Warmup(iterations = 5, time = 1)
@Fork(1)
class OfflineSearchBenchmark {
  import OfflineSearchBenchmark._

  @Param(Array("10", "100"))
  var documentCount: Int = _

  @Setup
  def setup(): Unit = {
    documents = TestDocument.generate(documentCount)
    zioElasticsearch = ZioElasticsearch.unsafeMake(OfflineBackend.layer(documents))

    require(search().size == documentCount, "Search did not decode every stubbed document")
  }

  @TearDown
  def tearDown(): Unit =
    zioElasticsearch.tearDown()

  @Benchmark
  def search(): Chunk[TestDocument] =
    BenchmarkClient.unsafeRun(zioElasticsearch.search(TestIndex, TestDocument.Terms(0)))

  @Benchmark
  def bulkUpsert(): Unit =
    BenchmarkClient.unsafeRun(zioElasticsearch.bulkUpsert(TestIndex, documents))

  @Benchmark
  def bulkUpdate(): Unit =
    BenchmarkClient.unsafeRun(zioElasticsearch.bulkUpdate(TestIndex, documents))

  private var documents: Chunk[TestDocument]     = _
  private var zioElasticsearch: ZioElasticsearch = _
}

object OfflineSearchBenchmark {
  private final val TestIndex: IndexName = IndexName("benchmark")
}
