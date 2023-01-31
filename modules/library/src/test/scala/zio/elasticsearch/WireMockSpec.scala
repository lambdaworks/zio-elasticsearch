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

import com.github.tomakehurst.wiremock.WireMockServer
import sttp.client3.httpclient.zio.HttpClientZioBackend
import zio.prelude.Newtype.unsafeWrap
import zio.test.ZIOSpecDefault
import zio.{TaskLayer, ZIO, ZLayer}

trait WireMockSpec extends ZIOSpecDefault {
  val index: IndexName = unsafeWrap(IndexName)("repositories")

  val repo: GitHubRepo =
    GitHubRepo(id = Some("123"), organization = "lambdaworks.io", name = "LambdaWorks", stars = 10, forks = 10)

  val port: Int = 9300

  val elasticsearchWireMockLayer: TaskLayer[ElasticExecutor] =
    HttpClientZioBackend
      .layer() >>> (ZLayer.succeed(ElasticConfig.apply("localhost", port)) >>> ElasticExecutor.live)

  val wireMockServerLayer: TaskLayer[WireMockServer] = {
    val server = ZIO.acquireRelease(
      ZIO.attemptBlocking {
        val server = new WireMockServer(port)

        server.start()
        server
      }
    )(server => ZIO.succeedBlocking(server.stop()))

    ZLayer.scoped(server)
  }
}
