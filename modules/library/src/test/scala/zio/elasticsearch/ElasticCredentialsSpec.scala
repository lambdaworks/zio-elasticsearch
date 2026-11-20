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
import zio.test.Assertion._
import zio.test.{Spec, TestEnvironment, ZTestLogger, assert, assertTrue}

import java.nio.charset.StandardCharsets
import java.util.Base64

object ElasticCredentialsSpec extends SttpBackendStubSpec {

  private val encodedCredentials: String =
    Base64.getEncoder.encodeToString(
      s"${credentials.username}:${credentials.password}".getBytes(StandardCharsets.UTF_8)
    )

  def spec: Spec[TestEnvironment, Any] =
    suite("ElasticCredentials")(
      suite("toString")(
        test("mask the password") {
          assert(credentials.toString)(
            equalTo("ElasticCredentials(username = elastic, password = ***)") &&
              not(containsString(credentials.password))
          )
        },
        test("mask the password when printing the config") {
          val config = ElasticConfig.Default.copy(credentials = Some(credentials))

          assert(config.toString)(
            containsString("ElasticCredentials(username = elastic, password = ***)") &&
              not(containsString(credentials.password))
          )
        }
      ),
      suite("request logging")(
        test("not log credentials") {
          for {
            _       <- Executor.execute(ElasticRequest.refresh(selectors = index))
            output  <- ZTestLogger.logOutput
            messages = output.map(_.message())
            requests = messages.filter(_.startsWith("[es-req]"))
          } yield assertTrue(
            requests.nonEmpty,
            requests.forall(_.contains("Authorization: ***")),
            !messages.exists(_.contains(encodedCredentials)),
            !messages.exists(_.contains(credentials.password))
          )
        }
      ).provideShared(elasticsearchSttpLayerWithCredentials)
    )
}
