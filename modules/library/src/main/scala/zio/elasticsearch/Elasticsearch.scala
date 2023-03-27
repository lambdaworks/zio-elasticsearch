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

import zio.elasticsearch.ElasticRequest.SearchRequest
import zio.elasticsearch.executor.Executor
import zio.elasticsearch.result.Item
import zio.schema.Schema
import zio.stream.{Stream, ZStream}
import zio.{RIO, Task, URLayer, ZIO, ZLayer}

trait Elasticsearch {
  def execute[A](request: ElasticRequest[A]): Task[A]

  def stream(request: SearchRequest): Stream[Throwable, Item]

  def stream(request: SearchRequest, config: StreamConfig): Stream[Throwable, Item]

  def streamAs[A: Schema](request: SearchRequest): Stream[Throwable, A]

  def streamAs[A: Schema](request: SearchRequest, config: StreamConfig): Stream[Throwable, A]
}

object Elasticsearch {
  def execute[A](request: ElasticRequest[A]): RIO[Elasticsearch, A] =
    ZIO.serviceWithZIO[Elasticsearch](_.execute(request))

  private[elasticsearch] def execute[A](request: ElasticRequest[A]): RIO[Elasticsearch, A] =
    ZIO.serviceWithZIO[Elasticsearch](_.execute(request))

  private[elasticsearch] def stream(request: SearchRequest): ZStream[Elasticsearch, Throwable, Item] =
    ZStream.serviceWithStream[Elasticsearch](_.stream(request))

  private[elasticsearch] def stream(
    request: SearchRequest,
    config: StreamConfig
  ): ZStream[Elasticsearch, Throwable, Item] =
    ZStream.serviceWithStream[Elasticsearch](_.stream(request, config))

  private[elasticsearch] def streamAs[A: Schema](request: SearchRequest): ZStream[Elasticsearch, Throwable, A] =
    ZStream.serviceWithStream[Elasticsearch](_.streamAs[A](request))

  lazy val layer: URLayer[Executor, Elasticsearch] =
    ZLayer.fromFunction { (executor: Executor) =>
      new Elasticsearch {
        def execute[A](request: ElasticRequest[A]): Task[A] =
          executor.execute(request)

        def stream(request: SearchRequest): Stream[Throwable, Item] =
          executor.stream(request)

        def stream(request: SearchRequest, config: StreamConfig): Stream[Throwable, Item] =
          executor.stream(request, config)

        def streamAs[A: Schema](request: SearchRequest): Stream[Throwable, A] =
          executor.streamAs[A](request)

        def streamAs[A: Schema](request: SearchRequest, config: StreamConfig): Stream[Throwable, A] =
          executor.streamAs[A](request, config)
      }
    }
}
