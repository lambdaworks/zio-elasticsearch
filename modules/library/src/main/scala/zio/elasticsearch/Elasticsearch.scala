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

import zio.elasticsearch.executor.Executor
import zio.{RIO, Task, URLayer, ZIO, ZLayer}

trait Elasticsearch {
  def execute[A](request: ElasticRequest[A]): Task[A]
}

object Elasticsearch {
  def execute[A](request: ElasticRequest[A]): RIO[Elasticsearch, A] =
    ZIO.serviceWithZIO[Elasticsearch](_.execute(request))

  lazy val layer: URLayer[Executor, Elasticsearch] =
    ZLayer.fromFunction { executor: Executor =>
      new Elasticsearch {
        def execute[A](request: ElasticRequest[A]): Task[A] = executor.execute(request)
      }
    }
}
