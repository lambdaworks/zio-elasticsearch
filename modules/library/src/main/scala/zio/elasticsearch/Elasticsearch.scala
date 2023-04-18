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

  /**
   * Executes the specified [[ElasticRequest]] and returns the response as a Task.
   *
   * @param request
   *   the [[ElasticRequest]] to execute
   * @tparam A
   *   the type of the expected response
   * @return
   *   returns a Task representing the response of the executed request.
   */
  def execute[A](request: ElasticRequest[A]): Task[A]

  /**
   * Executes the given [[SearchRequest]] and emits the search results as a Stream.
   *
   * @param request
   *   the [[SearchRequest]] to execute
   * @return
   *   returns a Stream of [[Item]].
   */
  def stream(request: SearchRequest): Stream[Throwable, Item]

  /**
   * Executes a [[SearchRequest]] with a given [[StreamConfig]] and emits the search results as a Stream.
   *
   * @param request
   *   the [[SearchRequest]] to execute
   * @param config
   *   the [[StreamConfig]] object that represents configuration options for the stream
   * @return
   *   returns a Stream of [[Item]].
   */
  def stream(request: SearchRequest, config: StreamConfig): Stream[Throwable, Item]

  /**
   * Executes a [[SearchRequest]] and returns a Stream of the resulting documents as `A`, where `A` is a case class that
   * has an implicit `Schema` instance in scope.
   *
   * @param request
   *   the [[SearchRequest]] to execute
   * @tparam A
   *   the type of documents to be returned
   * @return
   *   returns a Stream of the resulting documents as `A`.
   */
  def streamAs[A: Schema](request: SearchRequest): Stream[Throwable, A]

  /**
   * Executes a [[SearchRequest]] and returns a Stream of the resulting documents as `A`, where `A` is a case class that
   * has an implicit `Schema` instance in scope.
   *
   * @param request
   *   the [[SearchRequest]] to execute
   * @param config
   *   the [[StreamConfig]] object that represents configuration options for the stream
   * @tparam A
   *   the type of documents to be returned
   * @return
   *   returns a Stream of the resulting documents as `A`.
   */
  def streamAs[A: Schema](request: SearchRequest, config: StreamConfig): Stream[Throwable, A]
}

object Elasticsearch {
  final def execute[A](request: ElasticRequest[A]): RIO[Elasticsearch, A] =
    ZIO.serviceWithZIO[Elasticsearch](_.execute(request))

  final def stream(request: SearchRequest): ZStream[Elasticsearch, Throwable, Item] =
    ZStream.serviceWithStream[Elasticsearch](_.stream(request))

  final def stream(request: SearchRequest, config: StreamConfig): ZStream[Elasticsearch, Throwable, Item] =
    ZStream.serviceWithStream[Elasticsearch](_.stream(request, config))

  final def streamAs[A: Schema](request: SearchRequest): ZStream[Elasticsearch, Throwable, A] =
    ZStream.serviceWithStream[Elasticsearch](_.streamAs[A](request))

  final def streamAs[A: Schema](request: SearchRequest, config: StreamConfig): ZStream[Elasticsearch, Throwable, A] =
    ZStream.serviceWithStream[Elasticsearch](_.streamAs[A](request, config))

  lazy val layer: URLayer[Executor, Elasticsearch] =
    ZLayer.fromFunction { (executor: Executor) =>
      new Elasticsearch {
        final def execute[A](request: ElasticRequest[A]): Task[A] =
          executor.execute(request)

        final def stream(request: SearchRequest): Stream[Throwable, Item] =
          executor.stream(request)

        final def stream(request: SearchRequest, config: StreamConfig): Stream[Throwable, Item] =
          executor.stream(request, config)

        final def streamAs[A: Schema](request: SearchRequest): Stream[Throwable, A] =
          executor.streamAs[A](request)

        final def streamAs[A: Schema](request: SearchRequest, config: StreamConfig): Stream[Throwable, A] =
          executor.streamAs[A](request, config)
      }
    }
}
