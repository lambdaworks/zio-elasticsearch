package zio.elasticsearch

import zio.elasticsearch.ElasticError.DocumentRetrievingError.{DecoderError, DocumentNotFound}
import zio.test.Assertion.equalTo
import zio.test.TestAspect.nondeterministic
import zio.test._

object HttpExecutorSpec extends IntegrationSpec {

  override def spec: Spec[TestEnvironment, Any] =
    suite("HTTP Executor")(
      suite("get document by ID")(
        test("successfully get document by ID") {
          checkOnes(genDocId, genCustomer) { (documentId, customerDocument) =>
            val returnedDocument = for {
              _                <- ElasticRequest.upsert[CustomerDocument](docIndex, documentId, customerDocument).execute
              returnedDocument <- ElasticRequest.getById[CustomerDocument](docIndex, documentId).execute
            } yield returnedDocument

            assertZIO(returnedDocument)(Assertion.isRight(equalTo(customerDocument)))
          }
        },
        test("unsuccessfully get document by ID if it does not exists") {
          checkOnes(genDocId) { documentId =>
            assertZIO(ElasticRequest.getById[CustomerDocument](docIndex, documentId).execute)(
              Assertion.isLeft(equalTo(DocumentNotFound))
            )
          }
        },
        test("unsuccessfully get document by ID if decoder error happens") {
          checkOnes(genDocId, genEmployee) { (documentId, employeeDocument) =>
            val returnedDocument = for {
              _                <- ElasticRequest.upsert[EmployeeDocument](docIndex, documentId, employeeDocument).execute
              returnedDocument <- ElasticRequest.getById[CustomerDocument](docIndex, documentId).execute
            } yield returnedDocument

            assertZIO(returnedDocument)(Assertion.isLeft(equalTo(DecoderError(".address(missing)"))))
          }
        }
      ) @@ nondeterministic
    ).provideShared(elasticsearchLayer)
}
