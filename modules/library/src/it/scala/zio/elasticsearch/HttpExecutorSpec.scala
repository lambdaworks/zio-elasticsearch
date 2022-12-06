package zio.elasticsearch

import zio.elasticsearch.ElasticError.DocumentRetrievingError.{DecoderError, DocumentNotFound}
import zio.test.Assertion.equalTo
import zio.test.TestAspect.nondeterministic
import zio.test._

object HttpExecutorSpec extends ZIOSpecDefault with IntegrationSpec {

  override def spec: Spec[TestEnvironment, Any] =
    suite("HTTP Executor")(
      suite("get document by ID")(
        test("successfully get document by ID") {
          generateCustomerDocument.flatMap { expectedDoc =>
            val returnedDocument = for {
              docId       <- generateId
              _           <- ElasticRequest.upsert[CustomerDocument](docIndex, docId, expectedDoc).execute
              returnedDoc <- ElasticRequest.getById[CustomerDocument](docIndex, docId).execute
            } yield returnedDoc

            assertZIO(returnedDocument)(Assertion.isRight(equalTo(expectedDoc)))
          }
        },
        test("unsuccessfully get document by ID if it does not exists") {
          val returnedDocument = for {
            docId       <- generateId
            returnedDoc <- ElasticRequest.getById[CustomerDocument](docIndex, docId).execute
          } yield returnedDoc

          assertZIO(returnedDocument)(Assertion.isLeft(equalTo(DocumentNotFound)))
        },
        test("unsuccessfully get document by ID if decoder error happens") {
          val returnedDocument = for {
            docId       <- generateId
            newDoc      <- generateEmployeeDocument
            _           <- ElasticRequest.upsert[EmployeeDocument](docIndex, docId, newDoc).execute
            returnedDoc <- ElasticRequest.getById[CustomerDocument](docIndex, docId).execute
          } yield returnedDoc

          assertZIO(returnedDocument)(Assertion.isLeft(equalTo(DecoderError(".address(missing)"))))
        }
      ) @@ nondeterministic
    ).provideShared(elasticsearchLayer)
}
