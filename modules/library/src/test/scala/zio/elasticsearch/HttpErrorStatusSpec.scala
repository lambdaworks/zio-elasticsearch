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

import sttp.client4.httpclient.zio.HttpClientZioBackend
import sttp.client4.testing.ResponseStub
import sttp.model.StatusCode
import zio.elasticsearch.domain.TestDocument
import zio.elasticsearch.executor.Executor
import zio.elasticsearch.result.{ForbiddenException, UnauthorizedException}
import zio.test.Assertion._
import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assertZIO}
import zio.{TaskLayer, ZLayer}

object HttpErrorStatusSpec extends ZIOSpecDefault {

  private val index: IndexName = IndexName("repositories")

  private def executorLayer(code: StatusCode): TaskLayer[Executor] =
    (ZLayer.succeed(
      HttpClientZioBackend.stub.whenAnyRequest.thenRespond(
        ResponseStub.adjust(s"""{"error":{"type":"security_exception"},"status":${code.code}}""", code)
      )
    ) ++ ZLayer.succeed(ElasticConfig.Default)) >>> ElasticExecutor.live

  def spec: Spec[TestEnvironment, Any] =
    suite("HttpExecutor error status")(
      suite("401 Unauthorized")(
        test("fail with UnauthorizedException") {
          assertZIO(Executor.execute(ElasticRequest.refresh(selectors = index)).exit)(
            fails(equalTo(UnauthorizedException))
          )
        },
        test("fail with UnauthorizedException for a request with a decoded response") {
          assertZIO(Executor.execute(ElasticRequest.getById(index, DocumentId("1"))).documentAs[TestDocument].exit)(
            fails(equalTo(UnauthorizedException))
          )
        }
      ).provideShared(executorLayer(StatusCode.Unauthorized)),
      suite("403 Forbidden")(
        test("fail with ForbiddenException") {
          assertZIO(Executor.execute(ElasticRequest.refresh(selectors = index)).exit)(
            fails(equalTo(ForbiddenException))
          )
        },
        test("fail with ForbiddenException for a request with a decoded response") {
          assertZIO(Executor.execute(ElasticRequest.getById(index, DocumentId("1"))).documentAs[TestDocument].exit)(
            fails(equalTo(ForbiddenException))
          )
        }
      ).provideShared(executorLayer(StatusCode.Forbidden))
    )
}
