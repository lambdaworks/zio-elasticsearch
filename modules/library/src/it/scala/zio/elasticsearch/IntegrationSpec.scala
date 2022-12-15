package zio.elasticsearch

import sttp.client3.httpclient.zio.HttpClientZioBackend
import zio.ZLayer
import zio.prelude.Newtype.unsafeWrap
import zio.test.CheckVariants.CheckN
import zio.test.{Gen, ZIOSpecDefault, checkN}

trait IntegrationSpec extends ZIOSpecDefault {
  val elasticsearchLayer: ZLayer[Any, Throwable, ElasticExecutor] =
    HttpClientZioBackend.layer() >>> ElasticExecutor.local

  val index: IndexName = IndexName("users")

  def genIndexName: Gen[Any, IndexName] =
    Gen.stringBounded(10, 40)(Gen.alphaChar).map(name => unsafeWrap(IndexName)(name.toLowerCase))

  def genDocumentId: Gen[Any, DocumentId] = Gen.stringBounded(10, 40)(Gen.alphaNumericChar).map(DocumentId(_))

  def genCustomer: Gen[Any, CustomerDocument] = for {
    id      <- Gen.stringBounded(5, 10)(Gen.alphaNumericChar)
    name    <- Gen.stringBounded(5, 10)(Gen.alphaChar)
    address <- Gen.stringBounded(5, 10)(Gen.alphaNumericChar)
    balance <- Gen.bigDecimal(100, 10000)
  } yield CustomerDocument(id = id, name = name, address = address, balance = balance)

  def genEmployee: Gen[Any, EmployeeDocument] = for {
    id     <- Gen.stringBounded(5, 10)(Gen.alphaNumericChar)
    name   <- Gen.stringBounded(5, 10)(Gen.alphaChar)
    degree <- Gen.stringBounded(5, 10)(Gen.alphaChar)
  } yield EmployeeDocument(id = id, name = name, degree = degree)

  def checkOnce: CheckN = checkN(1)
}
