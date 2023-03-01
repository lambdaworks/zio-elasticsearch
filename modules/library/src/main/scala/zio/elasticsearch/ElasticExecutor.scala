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

import sttp.client3.SttpBackend
import zio.{RIO, Task, URLayer, ZIO, ZLayer}

private[elasticsearch] trait ElasticExecutor {
  def execute[A](request: ElasticRequest[A]): Task[A]
}

object ElasticExecutor {
  lazy val live: URLayer[ElasticConfig with SttpBackend[Task, Any], ElasticExecutor] =
    ZLayer.fromFunction(HttpElasticExecutor.apply _)

  lazy val local: URLayer[SttpBackend[Task, Any], ElasticExecutor] =
    ZLayer.succeed(ElasticConfig.Default) >>> live

  private[elasticsearch] def execute[A](request: ElasticRequest[A]): RIO[ElasticExecutor, A] =
    ZIO.serviceWithZIO[ElasticExecutor](_.execute(request))
}
