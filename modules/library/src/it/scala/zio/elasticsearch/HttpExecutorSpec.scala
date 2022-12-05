package zio.elasticsearch

import sttp.client3.httpclient.zio.HttpClientZioBackend
import zio.elasticsearch.ElasticError.DocumentRetrievingError.{DecoderError, DocumentNotFound}
import zio.test.Assertion.equalTo
import zio.test._

object HttpExecutorSpec extends ZIOSpecDefault {

  private val elasticsearchLayer = HttpClientZioBackend.layer() >>> ElasticExecutor.local

  private val docIndex = IndexName("users")
  private val document = CustomerDocument("123", "Doc123", "address 1", BigDecimal(100))

  override def spec: Spec[TestEnvironment, Any] =
    suite("HTTP Executor")(
      suite("get document by ID")(
        test("unsuccessfully get document by ID if it does not exists") {
          val doc = for {
            docId <- Gen.stringBounded(10, 39)(Gen.alphaNumericChar).map(DocumentId(_)).runHead
            doc   <- ElasticRequest.getById[CustomerDocument](docIndex, docId.get).execute
          } yield doc

          assertZIO(doc)(Assertion.isLeft(equalTo(DocumentNotFound)))
        },
        test("unsuccessfully get document by ID if decoder error happens") {
          val doc = for {
            docId <- Gen.stringBounded(10, 38)(Gen.alphaNumericChar).map(DocumentId(_)).runHead
            _     <- ElasticRequest.upsert[CustomerDocument](docIndex, docId.get, document).execute
            doc   <- ElasticRequest.getById[EmployeeDocument](docIndex, docId.get).execute
          } yield doc

          assertZIO(doc)(Assertion.isLeft(equalTo(DecoderError(".degree(missing)"))))
        },
        test("successfully get document by ID") {
          val doc = for {
            docId <- Gen.stringBounded(10, 37)(Gen.alphaNumericChar).map(DocumentId(_)).runHead
            _     <- ElasticRequest.upsert[CustomerDocument](docIndex, docId.get, document).execute
            doc   <- ElasticRequest.getById[CustomerDocument](docIndex, docId.get).execute
          } yield doc
          assertZIO(doc)(Assertion.isRight(equalTo(document)))
        }
      )
    ).provideShared(elasticsearchLayer)
}
