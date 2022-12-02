package zio.elasticsearch

import sttp.client3.httpclient.zio.HttpClientZioBackend
import zio.elasticsearch.ElasticError.DocumentRetrievingError.{DecoderError, DocumentNotFound}
import zio.schema.{DeriveSchema, Schema}
import zio.test._
import zio._
import java.util.UUID

object LiveSpec extends ZIOSpecDefault {

  private val elasticsearchLayer = HttpClientZioBackend.layer() >>> ElasticExecutor.local

  final case class TestDocument1(id: String, name: String, count: Int)
  final case class TestDocument2(desc: String)

  implicit val schema1: Schema[TestDocument1] = DeriveSchema.gen[TestDocument1]
  implicit val schema2: Schema[TestDocument2] = DeriveSchema.gen[TestDocument2]

  private val docIndex = IndexName("indexname")

  private val document = TestDocument1("123", "Doc123", 1)

  private val uuid: UIO[DocumentId] = ZIO.succeed(DocumentId(UUID.randomUUID().toString))

  override def spec: Spec[TestEnvironment, Any] =
    suite("live test")(
      suite("get document by id")(
        test("unsuccessfully get document by id if it does not exists") {
          val doc = for {
            docId <- uuid
            doc   <- ElasticRequest.getById[TestDocument1](docIndex, docId).routing("10").execute
          } yield doc

          assertZIO(doc)(Assertion.equalTo(Left(DocumentNotFound)))
        },
        test("unsuccessfully get document by id if decoder error happens") {
          val doc = for {
            docId <- uuid
            _     <- ElasticRequest.upsert[TestDocument1](docIndex, docId, document).routing("10").execute
            doc   <- ElasticRequest.getById[TestDocument2](docIndex, docId).routing("10").execute
          } yield doc

          assertZIO(doc)(Assertion.equalTo(Left(DecoderError(".desc(missing)"))))
        },
        test("successfully get document by id") {
          val doc = for {
            docId <- uuid
            _     <- Console.printLine(docId)
            _     <- ElasticRequest.upsert[TestDocument1](docIndex, docId, document).routing("10").execute
            doc   <- ElasticRequest.getById[TestDocument1](docIndex, docId).routing("10").execute
          } yield doc

          assertZIO(doc)(Assertion.equalTo(Right(document)))
        }
      )
    )
      .provideSomeLayerShared[TestEnvironment](
        elasticsearchLayer
      )
}
