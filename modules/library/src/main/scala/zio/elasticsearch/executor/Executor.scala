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

package zio.elasticsearch.executor

import zio.elasticsearch.ElasticRequest.SearchRequest
import zio.elasticsearch.result.Item
import zio.elasticsearch.{ElasticRequest, Executable, StreamConfig}
import zio.schema.Schema
import zio.stream.{Stream, ZStream}
import zio.{RIO, Task, ZIO}

private[elasticsearch] trait Executor {
  def execute[A](request: ElasticRequest[A, Executable.type]): Task[A]

  def stream(request: SearchRequest): Stream[Throwable, Item]

  def stream(request: SearchRequest, config: StreamConfig): Stream[Throwable, Item]

  def streamAs[A: Schema](request: SearchRequest): Stream[Throwable, A]

  def streamAs[A: Schema](request: SearchRequest, config: StreamConfig): Stream[Throwable, A]
}

private[elasticsearch] object Executor {
  private[elasticsearch] def execute[A](request: ElasticRequest[A, Executable.type]): RIO[Executor, A] =
    ZIO.serviceWithZIO[Executor](_.execute(request))

  private[elasticsearch] def stream(request: SearchRequest): ZStream[Executor, Throwable, Item] =
    ZStream.serviceWithStream[Executor](_.stream(request))

  private[elasticsearch] def stream(
    request: SearchRequest,
    config: StreamConfig
  ): ZStream[Executor, Throwable, Item] =
    ZStream.serviceWithStream[Executor](_.stream(request, config))

  private[elasticsearch] def streamAs[A: Schema](request: SearchRequest): ZStream[Executor, Throwable, A] =
    ZStream.serviceWithStream[Executor](_.streamAs[A](request))

  private[elasticsearch] def streamAs[A: Schema](
    request: SearchRequest,
    config: StreamConfig
  ): ZStream[Executor, Throwable, A] =
    ZStream.serviceWithStream[Executor](_.streamAs[A](request, config))
}
