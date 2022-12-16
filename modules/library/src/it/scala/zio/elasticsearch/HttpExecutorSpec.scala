package zio.elasticsearch

import sttp.client3.httpclient.zio.HttpClientZioBackend
import sttp.client3.{SttpBackend, basicRequest}
import sttp.model.StatusCode.Ok
import zio.elasticsearch.ElasticConfig.Default
import zio.{Task, ZIO}
import zio.elasticsearch.ElasticError.DocumentRetrievingError.{DecoderError, DocumentNotFound}
import zio.test.Assertion.{equalTo, isFalse, isLeft, isRight, isTrue, isUnit}
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

            assertZIO(result)(isRight(equalTo(customer)))
          }
        },
        test("return DocumentNotFound if the document does not exist") {
          checkOnce(genDocumentId) { documentId =>
            assertZIO(ElasticRequest.getById[CustomerDocument](index, documentId).execute)(
              isLeft(equalTo(DocumentNotFound))
            )
          }
        },
        test("fail with decoding error") {
          checkOnce(genDocumentId, genEmployee) { (documentId, employee) =>
            val result = for {
              _        <- ElasticRequest.upsert[EmployeeDocument](index, documentId, employee).execute
              document <- ElasticRequest.getById[CustomerDocument](index, documentId).execute
            } yield document

            assertZIO(result)(isLeft(equalTo(DecoderError(".address(missing)"))))
          }
        }
      ),
      suite("creating document")(
        test("successfully create document") {
          checkOnce(genCustomer) { customer =>
            val result = for {
              docId <- ElasticRequest.create[CustomerDocument](index, customer).execute
              res   <- ElasticRequest.getById[CustomerDocument](index, docId.getOrElse(DocumentId(""))).execute
            } yield res

            assertZIO(result)(isRight(equalTo(customer)))
          }
        },
        test("successfully create document with ID given") {
          checkOnce(genDocumentId, genCustomer) { (documentId, customer) =>
            val result = for {
              _   <- ElasticRequest.create[CustomerDocument](index, documentId, customer).execute
              doc <- ElasticRequest.getById[CustomerDocument](index, documentId).execute
            } yield doc

            assertZIO(result)(isRight(equalTo(customer)))
          }
        },
        test("fail to create document with ID given") {
          checkOnce(genDocumentId, genCustomer, genCustomer) { (documentId, customer1, customer2) =>
            val result = for {
              _   <- ElasticRequest.upsert[CustomerDocument](index, documentId, customer1).execute
              _   <- ElasticRequest.create[CustomerDocument](index, documentId, customer2).execute
              doc <- ElasticRequest.getById[CustomerDocument](index, documentId).execute
            } yield doc

            assertZIO(result)(isRight(equalTo(customer1)))
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

            assertZIO(result)(isRight(equalTo(customer)))
          }
        },
        test("successfully update document") {
          checkOnce(genDocumentId, genCustomer, genCustomer) { (documentId, customer1, customer2) =>
            val result = for {
              _   <- ElasticRequest.create[CustomerDocument](index, documentId, customer1).execute
              _   <- ElasticRequest.upsert[CustomerDocument](index, documentId, customer2).execute
              doc <- ElasticRequest.getById[CustomerDocument](index, documentId).execute
            } yield doc

            assertZIO(result)(isRight(equalTo(customer2)))
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

            assertZIO(result)(isTrue)
          }
        },
        test("return false if the document does not exist") {
          checkOnce(genDocumentId) { documentId =>
            assertZIO(ElasticRequest.exists(index, documentId).execute)(isFalse)
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

            assertZIO(result)(isRight(isUnit))
          }
        },
        test("return DocumentNotFound if the document does not exist") {
          checkOnce(genDocumentId) { documentId =>
            assertZIO(ElasticRequest.deleteById(index, documentId).execute)(isLeft(equalTo(DocumentNotFound)))
          }
        }
      ),
      suite("creating index")(
        test("return true if creation was successful") {
          checkOnce(genIndexName) { name =>
            val result = for {
              _    <- ElasticRequest.createIndex(name, None).execute
              sttp <- ZIO.service[SttpBackend[Task, Any]]
              indexExists <- basicRequest
                               .head(Default.uri.withPath(name.toString))
                               .send(sttp)
                               .map(_.code.equals(Ok))
            } yield indexExists

            assertZIO(result)(isTrue)
          }
        }
      ),
      suite("delete index")(
        test("return true if deletion was successful") {
          checkOnce(genIndexName) { name =>
            val result = for {
              _       <- ElasticRequest.createIndex(name, None).execute
              deleted <- ElasticRequest.deleteIndex(name).execute
            } yield deleted

            assertZIO(result)(isTrue)
          }
        },
        test("return false if deletion was not successful") {
          checkOnce(genIndexName) { name =>
            assertZIO(ElasticRequest.deleteIndex(name).execute)(isFalse)
          }
        }
      )
    ).provideShared(elasticsearchLayer, HttpClientZioBackend.layer()) @@ nondeterministic
}
