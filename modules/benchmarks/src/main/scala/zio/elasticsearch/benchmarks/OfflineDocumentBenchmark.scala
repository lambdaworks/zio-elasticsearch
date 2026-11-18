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
class OfflineDocumentBenchmark {
  import OfflineDocumentBenchmark._

  @Setup
  def setup(): Unit = {
    zioElasticsearch = ZioElasticsearch.unsafeMake(OfflineBackend.layer(Chunk.single(TestDocument.Single)))

    require(getById().contains(TestDocument.Single), "Get by ID did not decode the stubbed document")
  }

  @TearDown
  def tearDown(): Unit =
    zioElasticsearch.tearDown()

  @Benchmark
  def getById(): Option[TestDocument] =
    BenchmarkClient.unsafeRun(zioElasticsearch.getById(TestIndex, TestDocument.Single.id))

  @Benchmark
  def upsert(): Unit =
    BenchmarkClient.unsafeRun(zioElasticsearch.upsert(TestIndex, TestDocument.Single))

  private var zioElasticsearch: ZioElasticsearch = _
}

object OfflineDocumentBenchmark {
  private final val TestIndex: IndexName = IndexName("benchmark")
}
