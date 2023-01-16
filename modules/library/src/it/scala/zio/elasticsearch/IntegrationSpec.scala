package zio.elasticsearch

import sttp.client3.httpclient.zio.HttpClientZioBackend
import zio._
import zio.elasticsearch.ElasticQuery.matchAll
import zio.prelude.Newtype.unsafeWrap
import zio.test.Assertion.{containsString, hasMessage}
import zio.test.CheckVariants.CheckN
import zio.test.TestAspect.beforeAll
import zio.test.{Assertion, Gen, TestAspect, ZIOSpecDefault, checkN}

trait IntegrationSpec extends ZIOSpecDefault {

  val elasticsearchLayer: ZLayer[Any, Throwable, ElasticExecutor] =
    HttpClientZioBackend.layer() >>> ElasticExecutor.local

  val index: IndexName = IndexName("users")

  val createIndexTestName: IndexName = IndexName("create-index-test-name")

  val prepareElasticsearchIndexForTests: TestAspect[Nothing, Any, Throwable, Any] = beforeAll((for {
    _ <- ElasticRequest.createIndex(index, None).execute
    _ <- ElasticRequest.deleteByQuery(index, matchAll()).refreshTrue.execute
  } yield ()).provide(elasticsearchLayer))

  def genIndexName: Gen[Any, IndexName] =
    Gen.stringBounded(10, 40)(Gen.alphaChar).map(name => unsafeWrap(IndexName)(name.toLowerCase))

  def genDocumentId: Gen[Any, DocumentId] = Gen.stringBounded(10, 40)(Gen.alphaNumericChar).map(DocumentId(_))

  def genCustomer: Gen[Any, CustomerDocument] = for {
    id      <- Gen.stringBounded(5, 10)(Gen.alphaNumericChar)
    name    <- Gen.stringBounded(5, 10)(Gen.alphaChar)
    address <- Gen.stringBounded(5, 10)(Gen.alphaNumericChar)
    balance <- Gen.bigDecimal(100, 10000)
    age     <- Gen.int(18, 75)
  } yield CustomerDocument(id = id, name = name, address = address, balance = balance, age = age)

  def genEmployee: Gen[Any, EmployeeDocument] = for {
    id     <- Gen.stringBounded(5, 10)(Gen.alphaNumericChar)
    name   <- Gen.stringBounded(5, 10)(Gen.alphaChar)
    degree <- Gen.stringBounded(5, 10)(Gen.alphaChar)
    age    <- Gen.int(18, 75)
  } yield EmployeeDocument(id = id, name = name, degree = degree, age = age)

  def checkOnce: CheckN = checkN(1)

  def assertException(substring: String): Assertion[Throwable] = hasMessage(containsString(substring))
}
