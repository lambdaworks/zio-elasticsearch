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
import zio.stm.TMap
import zio.{Task, ULayer, ZIO, ZLayer}

trait ElasticExecutor {
  def execute[A](request: ElasticRequest[A, _]): Task[A]
}

object ElasticExecutor {
  lazy val live: ZLayer[ElasticConfig with SttpBackend[Task, Any], Throwable, ElasticExecutor] =
    ZLayer {
      for {
        conf <- ZIO.service[ElasticConfig]
        sttp <- ZIO.service[SttpBackend[Task, Any]]
      } yield HttpElasticExecutor(conf, sttp)
    }

  lazy val local: ZLayer[SttpBackend[Task, Any], Throwable, ElasticExecutor] =
    ZLayer.succeed(ElasticConfig.Default) >>> live

  lazy val test: ULayer[TestExecutor] =
    ZLayer(TMap.empty[IndexName, TMap[DocumentId, Document]].map(TestExecutor).commit)
}
