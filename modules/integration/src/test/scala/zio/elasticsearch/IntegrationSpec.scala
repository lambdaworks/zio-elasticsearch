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

import sttp.client3.httpclient.zio.HttpClientZioBackend
import zio._
import zio.elasticsearch.ElasticQuery.matchAll
import zio.elasticsearch.data.GeoPoint
import zio.elasticsearch.domain._
import zio.elasticsearch.executor.Executor
import zio.elasticsearch.utils.unsafeWrap
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

  val updateByQueryIndex: IndexName = IndexName("update-by-query-index")

  val geoDistanceIndex: IndexName = IndexName("geo-distance-index")

  val refreshFailIndex: IndexName = IndexName("refresh-fail")

  val prepareElasticsearchIndexForTests: TestAspect[Nothing, Any, Throwable, Any] = beforeAll((for {
    _ <- Executor.execute(ElasticRequest.createIndex(index))
    _ <- Executor.execute(ElasticRequest.deleteByQuery(index, matchAll).refreshTrue)
  } yield ()).provide(elasticsearchLayer))

  def genIndexName: Gen[Any, IndexName] =
    Gen.stringBounded(10, 40)(Gen.alphaChar).map(name => unsafeWrap(name.toLowerCase)(IndexName))

  def genDocumentId: Gen[Any, DocumentId] =
    Gen.stringBounded(10, 40)(Gen.alphaNumericChar).map(DocumentId(_))

  def genGeoPoint: Gen[Any, GeoPoint] =
    for {
      latitude  <- Gen.bigDecimal(10, 90).map(_.setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble)
      longitude <- Gen.bigDecimal(10, 90).map(_.setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble)
    } yield GeoPoint(latitude, longitude)

  def genTestDocument: Gen[Any, TestDocument] = for {
    stringField     <- Gen.stringBounded(5, 10)(Gen.alphaChar)
    dateField       <- Gen.localDate(LocalDate.parse("2010-12-02"), LocalDate.parse("2022-12-05"))
    subDocumentList <- Gen.listOfBounded(1, 3)(genTestSubDocument)
    intField        <- Gen.int(1, 2000)
    doubleField     <- Gen.double(100, 2000)
    booleanField    <- Gen.boolean
    geoPointField   <- genGeoPoint
  } yield TestDocument(
    stringField = stringField,
    dateField = dateField,
    subDocumentList = subDocumentList,
    intField = intField,
    doubleField = doubleField,
    booleanField = booleanField,
    geoPointField = geoPointField
  )

  def genTestSubDocument: Gen[Any, TestSubDocument] = for {
    stringField1 <- Gen.stringBounded(5, 10)(Gen.alphaChar)
    stringField2 <- Gen.stringBounded(5, 10)(Gen.alphaChar)
    longField    <- Gen.long(1, 75)
    intField     <- Gen.int(1, 200)
  } yield TestSubDocument(
    stringField = stringField1,
    nestedField = TestNestedField(stringField2, longField),
    intField = intField,
    intFieldList = Nil
  )

  def checkOnce: CheckN = checkN(1)

  def assertException(substring: String): Assertion[Throwable] = hasMessage(containsString(substring))
}
