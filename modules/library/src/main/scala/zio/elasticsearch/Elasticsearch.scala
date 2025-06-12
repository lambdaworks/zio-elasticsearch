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
   * Executes the given [[ElasticRequest]].
   *
   * @param request
   *   the [[zio.elasticsearch.ElasticRequest]] to execute, this request must be of type `Executable`
   * @tparam A
   *   the type of the expected response
   * @return
   *   a [[Task]] representing the response of the executed request.
   */
  def execute[A](request: ElasticRequest[A, Executable.type]): Task[A]

  /**
   * Executes the given [[zio.elasticsearch.ElasticRequest.SearchRequest]] as a stream.
   *
   * @param request
   *   the [[zio.elasticsearch.ElasticRequest.SearchRequest]] to execute
   * @return
   *   a stream of [[zio.elasticsearch.result.Item]].
   */
  def stream(request: SearchRequest): Stream[Throwable, Item]

  /**
   * Executes a [[zio.elasticsearch.ElasticRequest.SearchRequest]] with a given [[StreamConfig]].
   *
   * @param request
   *   the [[zio.elasticsearch.ElasticRequest.SearchRequest]] to execute
   * @param config
   *   the [[zio.elasticsearch.StreamConfig]] object that represents configuration options for the stream
   * @return
   *   a stream of [[zio.elasticsearch.result.Item]].
   */
  def stream(request: SearchRequest, config: StreamConfig): Stream[Throwable, Item]

  /**
   * Executes a [[zio.elasticsearch.ElasticRequest.SearchRequest]] and stream resulting documents as `A`, where `A` is a
   * case class that has an implicit `Schema` instance in scope.
   *
   * @param request
   *   the [[zio.elasticsearch.ElasticRequest.SearchRequest]] to execute
   * @tparam A
   *   the type of documents to be returned
   * @return
   *   a stream of the resulting documents as `A`.
   */
  def streamAs[A: Schema](request: SearchRequest): Stream[Throwable, A]

  /**
   * Executes a [[zio.elasticsearch.ElasticRequest.SearchRequest]] and stream resulting documents as `A`, where `A` is a
   * case class that has an implicit `Schema` instance in scope.
   *
   * @param request
   *   the [[zio.elasticsearch.ElasticRequest.SearchRequest]] to execute
   * @param config
   *   the [[zio.elasticsearch.StreamConfig]] object that represents configuration options for the stream
   * @tparam A
   *   the type of documents to be returned
   * @return
   *   a stream of the resulting documents as `A`.
   */
  def streamAs[A: Schema](request: SearchRequest, config: StreamConfig): Stream[Throwable, A]
}

object Elasticsearch {
  final def execute[A](request: ElasticRequest[A, Executable.type]): RIO[Elasticsearch, A] =
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
        final def execute[A](request: ElasticRequest[A, Executable.type]): Task[A] =
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
