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
import zio._
import zio.elasticsearch.ElasticQuery.matchAll
import zio.elasticsearch.executor.Executor
import zio.test.Assertion.{containsString, hasMessage}
import zio.test.CheckVariants.CheckN
import zio.test.TestAspect.beforeAll
import zio.test.{Assertion, Gen, TestAspect, ZIOSpecDefault, checkN}

import java.time.LocalDate

trait IntegrationSpec extends ZIOSpecDefault {

  val elasticsearchLayer: TaskLayer[Executor] = HttpClientZioBackend.layer() >>> ElasticExecutor.local

  val index: IndexName = IndexName("users")

  val deleteByQueryIndex: IndexName = IndexName("delete-by-query-index")

  val firstSearchIndex: IndexName = IndexName("search-index-1")

  val secondSearchIndex: IndexName = IndexName("search-index-2")

  val createIndexTestName: IndexName = IndexName("create-index-test-name")

  val firstCountIndex: IndexName = IndexName("count-index-1")

  val secondCountIndex: IndexName = IndexName("count-index-2")

  val prepareElasticsearchIndexForTests: TestAspect[Nothing, Any, Throwable, Any] = beforeAll((for {
    _ <- Executor.execute(ElasticRequest.createIndex(index))
    _ <- Executor.execute(ElasticRequest.deleteByQuery(index, matchAll).refreshTrue)
  } yield ()).provide(elasticsearchLayer))

  def genIndexName: Gen[Any, IndexName] =
    Gen.stringBounded(10, 40)(Gen.alphaChar).map(name => IndexName(name.toLowerCase))

  def genDocumentId: Gen[Any, DocumentId] = Gen.stringBounded(10, 40)(Gen.alphaNumericChar).map(DocumentId(_))

  def genCustomer: Gen[Any, CustomerDocument] = for {
    id      <- Gen.stringBounded(5, 10)(Gen.alphaNumericChar)
    name    <- Gen.stringBounded(5, 10)(Gen.alphaChar)
    address <- Gen.stringBounded(5, 10)(Gen.alphaNumericChar)
    balance <- Gen.bigDecimal(100, 10000)
    age     <- Gen.int(18, 75)
  } yield CustomerDocument(id = id, name = name, address = address, balance = balance, age = age)

  def genEmployee: Gen[Any, EmployeeDocument] = for {
    id        <- Gen.stringBounded(5, 10)(Gen.alphaNumericChar)
    name      <- Gen.stringBounded(5, 10)(Gen.alphaChar)
    degree    <- Gen.stringBounded(5, 10)(Gen.alphaChar)
    birthDate <- Gen.localDate(LocalDate.parse("1991-12-02"), LocalDate.parse("1999-12-05"))
    age       <- Gen.int(18, 75)
    sectorId1 <- Gen.numericChar
  } yield EmployeeDocument(
    id = id,
    name = name,
    degree = degree,
    sectorsIds = List(sectorId1 + 10, sectorId1, sectorId1 - 5),
    age = age,
    birthDate = birthDate
  )

  def checkOnce: CheckN = checkN(1)

  def assertException(substring: String): Assertion[Throwable] = hasMessage(containsString(substring))
}
