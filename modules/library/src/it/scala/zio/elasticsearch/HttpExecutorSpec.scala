package zio.elasticsearch

import zio.elasticsearch.ElasticError.DocumentRetrievingError.{DecoderError, DocumentNotFound}
import zio.test.Assertion.{equalTo, isUnit}
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
      ),
      suite("creating document")(
        test("successfully create document") {
          checkOnce(genCustomer) { customer =>
            assertZIO(ElasticRequest.create[CustomerDocument](index, customer).execute)(Assertion.isSome)
          }
        },
        test("successfully create document with ID given") {
          checkOnce(genDocumentId, genCustomer) { (documentId, customer) =>
            val result = for {
              _   <- ElasticRequest.create[CustomerDocument](index, documentId, customer).execute
              doc <- ElasticRequest.getById[CustomerDocument](index, documentId).execute
            } yield doc

            assertZIO(result)(Assertion.isRight(equalTo(customer)))
          }
        },
        test("fail to create document with ID given") { // TODO: change when introduce error for this case
          checkOnce(genDocumentId, genCustomer, genCustomer) { (documentId, customer1, customer2) =>
            val result = for {
              _   <- ElasticRequest.upsert[CustomerDocument](index, documentId, customer1).execute
              _   <- ElasticRequest.create[CustomerDocument](index, documentId, customer2).execute
              doc <- ElasticRequest.getById[CustomerDocument](index, documentId).execute
            } yield doc

            assertZIO(result)(Assertion.isRight(equalTo(customer1)))
          }
        }
      ),
      suite("creating or updating document")(
        test("successfully create document") {
          checkOnce(genDocumentId, genCustomer) { (documentId, customer) =>
            val result = for {
              _   <- ElasticRequest.upsert[CustomerDocument](index, documentId, customer).execute
              doc <- ElasticRequest.getById[CustomerDocument](index, documentId).execute
            } yield doc

            assertZIO(result)(Assertion.isRight(equalTo(customer)))
          }
        },
        test("successfully update document") {
          checkOnce(genDocumentId, genCustomer, genCustomer) { (documentId, customer1, customer2) =>
            val result = for {
              _   <- ElasticRequest.create[CustomerDocument](index, documentId, customer1).execute
              _   <- ElasticRequest.upsert[CustomerDocument](index, documentId, customer2).execute
              doc <- ElasticRequest.getById[CustomerDocument](index, documentId).execute
            } yield doc

            assertZIO(result)(Assertion.isRight(equalTo(customer2)))
          }
        }
      ),
      suite("finding document")(
        test("return true if the document exists") {
          checkOnce(genDocumentId, genCustomer) { (documentId, customer) =>
            val result = for {
              _      <- ElasticRequest.upsert[CustomerDocument](index, documentId, customer).execute
              exists <- ElasticRequest.exists(index, documentId).execute
            } yield exists

            assertZIO(result)(equalTo(true))
          }
        },
        test("return false if the document does not exist") {
          checkOnce(genDocumentId) { documentId =>
            assertZIO(ElasticRequest.exists(index, documentId).execute)(equalTo(false))
          }
        }
      ),
      suite("deleting document by ID")(
        test("return unit if document deletion was successful") {
          checkOnce(genDocumentId, genCustomer) { (documentId, customer) =>
            val result = for {
              _   <- ElasticRequest.upsert[CustomerDocument](index, documentId, customer).execute
              res <- ElasticRequest.deleteById(index, documentId).execute
            } yield res

            assertZIO(result)(Assertion.isRight(isUnit))
          }
        },
        test("return DocumentNotFound if the document does not exist") {
          checkOnce(genDocumentId) { documentId =>
            val result = for {
              doc <- ElasticRequest.deleteById(index, documentId).execute
            } yield doc

            assertZIO(result)(Assertion.isLeft(equalTo(DocumentNotFound)))
          }
        }
      ),
      suite("creating index")(
        test("return unit if creation was successful") {
          checkOnce(genIndexName) { indexName =>
            assertZIO(ElasticRequest.createIndex(indexName, None).execute)(equalTo(()))
          }
        }
      ),
      suite("delete index")(
        test("return unit if deletion was successful") {
          checkOnce(genIndexName) { indexName =>
            assertZIO(ElasticRequest.deleteIndex(indexName).execute)(equalTo(()))
          }
        }
      )
    ).provideShared(elasticsearchLayer) @@ nondeterministic
}
