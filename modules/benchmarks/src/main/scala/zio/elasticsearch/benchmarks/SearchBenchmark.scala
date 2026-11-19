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
import sttp.client4.httpclient.zio.HttpClientZioBackend
import zio.elasticsearch.{ElasticBackend, IndexName}
import zio.{Scope => _, _}

import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
@Measurement(iterations = 15)
@Warmup(iterations = 15)
@Fork(2)
class SearchBenchmark {
  import SearchBenchmark._

  @Param(Array("10"))
  var parallelism: Int = _

  @Param(Array("50"))
  var repetitions: Int = _

  @Setup
  def setup(): Unit = {
    zioElasticsearch = ZioElasticsearch.unsafeMake(ElasticBackend.layer())
    zioElasticsearchSttpBackend = ZioElasticsearch.unsafeMake(HttpClientZioBackend.layer())
    elastic4s = Elastic4s.unsafeMake()
    elasticsearchJava = ElasticsearchJava.unsafeMake()
    highLevelRestClient = HighLevelRestClient.unsafeMake()

    BenchmarkClient.unsafeRun(zioElasticsearch.prepareIndex(TestIndex, TestDocument.generate(DocumentCount)))
  }

  @TearDown
  def tearDown(): Unit = {
    zioElasticsearch.tearDown()
    zioElasticsearchSttpBackend.tearDown()
    elastic4s.tearDown()
    elasticsearchJava.tearDown()
    highLevelRestClient.tearDown()
  }

  @Benchmark
  def zio(): Unit = run(zioElasticsearch)

  @Benchmark
  def zioSttpBackend(): Unit = run(zioElasticsearchSttpBackend)

  @Benchmark
  def elastic4sZio(): Unit = run(elastic4s)

  @Benchmark
  def javaClient(): Unit = run(elasticsearchJava)

  @Benchmark
  def javaHighLevelRestClient(): Unit = run(highLevelRestClient)

  private def run(client: BenchmarkClient): Unit =
    BenchmarkClient.unsafeRun {
      val task  = ZIO.suspendSucceed(client.search(TestIndex, TestDocument.Terms(0)))
      val tasks = Chunk.fill(parallelism)(task.repeatN(repetitions))

      ZIO.collectAllParDiscard(tasks)
    }

  private var zioElasticsearch: ZioElasticsearch            = _
  private var zioElasticsearchSttpBackend: ZioElasticsearch = _
  private var elastic4s: Elastic4s                          = _
  private var elasticsearchJava: ElasticsearchJava          = _
  private var highLevelRestClient: HighLevelRestClient      = _
}

object SearchBenchmark {
  private final val DocumentCount        = 1000
  private final val TestIndex: IndexName = IndexName("benchmark-search")
}
