package zio.elasticsearch

import sttp.client3.httpclient.zio.HttpClientZioBackend
import zio.{ZIO, ZLayer}
import zio.test.Gen

trait IntegrationSpec {
  private[elasticsearch] val elasticsearchLayer: ZLayer[Any, Throwable, ElasticExecutor] =
    HttpClientZioBackend.layer() >>> ElasticExecutor.local

  private[elasticsearch] val docIndex: IndexName = IndexName("users")

  private[elasticsearch] def generateId: ZIO[Any, Nothing, DocumentId] =
    Gen.stringBounded(10, 40)(Gen.alphaNumericChar).runHead.map(maybeId => DocumentId(maybeId.getOrElse("DocumentId")))

  private[elasticsearch] def generateCustomerDocument: ZIO[Any, Nothing, CustomerDocument] = for {
    id      <- Gen.stringBounded(5, 10)(Gen.alphaNumericChar).runHead
    name    <- Gen.stringBounded(5, 10)(Gen.alphaChar).runHead
    address <- Gen.stringBounded(5, 10)(Gen.alphaNumericChar).runHead
    balance <- Gen.bigDecimal(100, 10000).runHead
  } yield CustomerDocument(
    id = id.getOrElse("123"),
    name = name.getOrElse("CustomerDocument"),
    address = address.getOrElse("address 1"),
    balance = balance.getOrElse(BigDecimal(100))
  )

  private[elasticsearch] def generateEmployeeDocument: ZIO[Any, Nothing, EmployeeDocument] = for {
    id     <- Gen.stringBounded(5, 10)(Gen.alphaNumericChar).runHead
    name   <- Gen.stringBounded(5, 10)(Gen.alphaChar).runHead
    degree <- Gen.stringBounded(5, 10)(Gen.alphaChar).runHead
  } yield EmployeeDocument(
    id = id.getOrElse("123"),
    name = name.getOrElse("EmployeeDocument"),
    degree = degree.getOrElse("degree")
  )
}
