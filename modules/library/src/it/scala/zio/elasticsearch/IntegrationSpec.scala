package zio.elasticsearch

import sttp.client3.httpclient.zio.HttpClientZioBackend
import zio.ZLayer
import zio.test.CheckVariants.CheckN
import zio.test.{Gen, ZIOSpecDefault, checkN}

trait IntegrationSpec extends ZIOSpecDefault {
  private[elasticsearch] val elasticsearchLayer: ZLayer[Any, Throwable, ElasticExecutor] =
    HttpClientZioBackend.layer() >>> ElasticExecutor.local

  private[elasticsearch] val docIndex: IndexName = IndexName("users")

  private[elasticsearch] def genDocId: Gen[Any, DocumentId] =
    Gen.stringBounded(10, 40)(Gen.alphaNumericChar).map(DocumentId(_))

  private[elasticsearch] def genCustomer: Gen[Any, CustomerDocument] = for {
    id      <- Gen.stringBounded(5, 10)(Gen.alphaNumericChar)
    name    <- Gen.stringBounded(5, 10)(Gen.alphaChar)
    address <- Gen.stringBounded(5, 10)(Gen.alphaNumericChar)
    balance <- Gen.bigDecimal(100, 10000)
  } yield CustomerDocument(
    id = id,
    name = name,
    address = address,
    balance = balance
  )

  private[elasticsearch] def genEmployee: Gen[Any, EmployeeDocument] = for {
    id     <- Gen.stringBounded(5, 10)(Gen.alphaNumericChar)
    name   <- Gen.stringBounded(5, 10)(Gen.alphaChar)
    degree <- Gen.stringBounded(5, 10)(Gen.alphaChar)
  } yield EmployeeDocument(
    id = id,
    name = name,
    degree = degree
  )

  private[elasticsearch] def checkOnes: CheckN = checkN(1)
}
