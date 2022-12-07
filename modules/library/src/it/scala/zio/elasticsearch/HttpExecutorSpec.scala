package zio.elasticsearch

import zio.elasticsearch.ElasticError.DocumentRetrievingError.{DecoderError, DocumentNotFound}
import zio.test.Assertion.equalTo
import zio.test.TestAspect.nondeterministic
import zio.test._

object HttpExecutorSpec extends IntegrationSpec {

  override def spec: Spec[TestEnvironment, Any] =
    suite("HTTP Executor")(
      suite("retrieving document by ID")(
        test("successfully return document") {
          checkOnce(genDocumentId, genCustomer) { (documentId, customer) =>
            val result = for {
              _        <- ElasticRequest.upsert[CustomerDocument](index, documentId, customer).execute
              document <- ElasticRequest.getById[CustomerDocument](index, documentId).execute
            } yield document

            assertZIO(result)(Assertion.isRight(equalTo(customer)))
          }
        },
        test("return DocumentNotFound if the document does not exist") {
          checkOnce(genDocumentId) { documentId =>
            assertZIO(ElasticRequest.getById[CustomerDocument](index, documentId).execute)(
              Assertion.isLeft(equalTo(DocumentNotFound))
            )
          }
        },
        test("fail with decoding error") {
          checkOnce(genDocumentId, genEmployee) { (documentId, employee) =>
            val result = for {
              _        <- ElasticRequest.upsert[EmployeeDocument](index, documentId, employee).execute
              document <- ElasticRequest.getById[CustomerDocument](index, documentId).execute
            } yield document

            assertZIO(result)(Assertion.isLeft(equalTo(DecoderError(".address(missing)"))))
          }
        }
      ) @@ nondeterministic
    ).provideShared(elasticsearchLayer)
}
