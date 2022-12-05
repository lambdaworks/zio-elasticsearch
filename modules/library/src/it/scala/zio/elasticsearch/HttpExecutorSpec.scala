package zio.elasticsearch

import sttp.client3.httpclient.zio.HttpClientZioBackend
import zio.elasticsearch.ElasticError.DocumentRetrievingError.{DecoderError, DocumentNotFound}
import zio.elasticsearch.UserDocument.{schema1, schema2}
import zio.test.Assertion.equalTo
import zio.test._

object HttpExecutorSpec extends ZIOSpecDefault {

  private val elasticsearchLayer = HttpClientZioBackend.layer() >>> ElasticExecutor.local

  private val docIndex = IndexName("indexname")
  private val document = UserDocument1("123", "Doc123", 1)

  override def spec: Spec[TestEnvironment, Any] =
    suite("Http executor test")(
      suite("get document by id")(
        test("unsuccessfully get document by id if it does not exists") {
          val doc = for {
            docId <- Gen.stringBounded(10, 39)(Gen.alphaNumericChar).map(DocumentId(_)).runHead
            doc   <- ElasticRequest.getById[UserDocument1](docIndex, docId.get).execute
          } yield doc

          assertZIO(doc)(Assertion.isLeft(equalTo(DocumentNotFound)))
        },
        test("unsuccessfully get document by id if decoder error happens") {
          val doc = for {
            docId <- Gen.stringBounded(10, 38)(Gen.alphaNumericChar).map(DocumentId(_)).runHead
            _     <- ElasticRequest.upsert[UserDocument1](docIndex, docId.get, document).execute
            doc   <- ElasticRequest.getById[UserDocument2](docIndex, docId.get).execute
          } yield doc
          // TODO Is it enough just to have docId.get?
          assertZIO(doc)(Assertion.isLeft(equalTo(DecoderError(".desc(missing)"))))
        },
        test("successfully get document by id") {
          val doc = for {
            docId <- Gen.stringBounded(10, 37)(Gen.alphaNumericChar).map(DocumentId(_)).runHead
            _     <- ElasticRequest.upsert[UserDocument1](docIndex, docId.get, document).execute
            doc   <- ElasticRequest.getById[UserDocument1](docIndex, docId.get).execute
          } yield doc
          ()
          assertZIO(doc)(Assertion.isRight(equalTo(document)))
        }
      )
    )
      .provideShared(
        elasticsearchLayer
      )
}
