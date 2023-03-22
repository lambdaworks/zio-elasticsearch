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

import zio.Task
import zio.elasticsearch.ElasticRequest.SearchRequest
import zio.elasticsearch.result.Item
import zio.elasticsearch.{ElasticRequest, StreamConfig}
import zio.schema.Schema
import zio.stream.Stream

private[elasticsearch] trait ElasticExecutor {
  def execute[A](request: ElasticRequest[A]): Task[A]

  def stream(request: SearchRequest): Stream[Throwable, Item]

  def stream(request: SearchRequest, config: StreamConfig): Stream[Throwable, Item]

  def streamAs[A: Schema](request: SearchRequest): Stream[Throwable, A]

  def streamAs[A: Schema](request: SearchRequest, config: StreamConfig): Stream[Throwable, A]
}
