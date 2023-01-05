package zio.elasticsearch

import zio.elasticsearch.CreationOutcome.{AlreadyExists, Created}
import zio.elasticsearch.DeletionOutcome.{Deleted, NotFound}
import zio.test.Assertion._
import zio.test.TestAspect.nondeterministic
import zio.test._

object HttpExecutorSpec extends IntegrationSpec {

  override def spec: Spec[TestEnvironment, Any] =
    suite("HTTP Executor")(
      suite("creating document")(
        test("successfully create document") {
          checkOnce(genCustomer) { customer =>
            val result = for {
              docId <- ElasticRequest.create[CustomerDocument](index, customer).execute
              res   <- ElasticRequest.getById[CustomerDocument](index, docId).execute
            } yield res

            assertZIO(result)(isSome(equalTo(customer)))
          }
        },
        test("successfully create document with ID given") {
          checkOnce(genDocumentId, genCustomer) { (documentId, customer) =>
            assertZIO(ElasticRequest.create[CustomerDocument](index, documentId, customer).execute)(equalTo(Created))
          }
        },
        test("return 'AlreadyExists' if document with given ID already exists") {
          checkOnce(genDocumentId, genCustomer, genCustomer) { (documentId, customer1, customer2) =>
            val result = for {
              _   <- ElasticRequest.upsert[CustomerDocument](index, documentId, customer1).execute
              res <- ElasticRequest.create[CustomerDocument](index, documentId, customer2).execute
            } yield res

            assertZIO(result)(equalTo(AlreadyExists))
          }
        }
      ),
      suite("creating index")(
        test("successfully create index") {
          checkOnce(genIndexName) { name =>
            assertZIO(ElasticRequest.createIndex(name, None).execute)(equalTo(Created))
          }
        },
        test("return 'AlreadyExists' if index already exists") {
          checkOnce(genIndexName) { name =>
            val result = for {
              _   <- ElasticRequest.createIndex(name, None).execute
              res <- ElasticRequest.createIndex(name, None).execute
            } yield res

            assertZIO(result)(equalTo(AlreadyExists))
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

            assertZIO(result)(isSome(equalTo(customer)))
          }
        },
        test("successfully update document") {
          checkOnce(genDocumentId, genCustomer, genCustomer) { (documentId, customer1, customer2) =>
            val result = for {
              _   <- ElasticRequest.create[CustomerDocument](index, documentId, customer1).execute
              _   <- ElasticRequest.upsert[CustomerDocument](index, documentId, customer2).execute
              doc <- ElasticRequest.getById[CustomerDocument](index, documentId).execute
            } yield doc

            assertZIO(result)(isSome(equalTo(customer2)))
          }
        }
      ),
      suite("deleting document by ID")(
        test("successfully delete existing document") {
          checkOnce(genDocumentId, genCustomer) { (documentId, customer) =>
            val result = for {
              _   <- ElasticRequest.upsert[CustomerDocument](index, documentId, customer).execute
              res <- ElasticRequest.deleteById(index, documentId).execute
            } yield res

            assertZIO(result)(equalTo(Deleted))
          }
        },
        test("return 'NotFound' if the document does not exist") {
          checkOnce(genDocumentId) { documentId =>
            assertZIO(ElasticRequest.deleteById(index, documentId).execute)(equalTo(NotFound))
          }
        }
      ),
      suite("delete index")(
        test("successfully delete existing index") {
          checkOnce(genIndexName) { name =>
            val result = for {
              _   <- ElasticRequest.createIndex(name, None).execute
              res <- ElasticRequest.deleteIndex(name).execute
            } yield res

            assertZIO(result)(equalTo(Deleted))
          }
        },
        test("return 'NotFound' if index does not exists") {
          checkOnce(genIndexName) { name =>
            assertZIO(ElasticRequest.deleteIndex(name).execute)(equalTo(NotFound))
          }
        }
      ),
      suite("finding document")(
        test("return true if the document exists") {
          checkOnce(genDocumentId, genCustomer) { (documentId, customer) =>
            val result = for {
              _   <- ElasticRequest.upsert[CustomerDocument](index, documentId, customer).execute
              res <- ElasticRequest.exists(index, documentId).execute
            } yield res

            assertZIO(result)(isTrue)
          }
        },
        test("return false if the document does not exist") {
          checkOnce(genDocumentId) { documentId =>
            assertZIO(ElasticRequest.exists(index, documentId).execute)(isFalse)
          }
        }
      ),
      suite("retrieving document by ID")(
        test("successfully return document") {
          checkOnce(genDocumentId, genCustomer) { (documentId, customer) =>
            val result = for {
              _   <- ElasticRequest.upsert[CustomerDocument](index, documentId, customer).execute
              res <- ElasticRequest.getById[CustomerDocument](index, documentId).execute
            } yield res

            assertZIO(result)(isSome(equalTo(customer)))
          }
        },
        test("return None if the document does not exist") {
          checkOnce(genDocumentId) { documentId =>
            assertZIO(ElasticRequest.getById[CustomerDocument](index, documentId).execute)(isNone)
          }
        },
        test("fail with throwable if decoding fails") {
          checkOnce(genDocumentId, genEmployee) { (documentId, employee) =>
            val result = for {
              _   <- ElasticRequest.upsert[EmployeeDocument](index, documentId, employee).execute
              res <- ElasticRequest.getById[CustomerDocument](index, documentId).execute
            } yield res

            assertZIO(result.exit)(fails(isSubtype[Exception](assertException("Decoding error: .address(missing)"))))
          }
        }
      ),
      suite("searching documents")(
        test("search for document using range query") {
          checkOnce(genDocumentId, genCustomer) { (documentId, customer) =>
            val result = for {
              _    <- ElasticRequest.upsert[CustomerDocument](index, documentId, customer).execute
              query = ElasticQuery.range("balance").gte(100)
              res  <- ElasticRequest.search[CustomerDocument](index, query).execute
            } yield res

            assertZIO(result)(isNonEmpty)
          }
        },
        test("fail if any of results cannot be decoded") {
          checkOnce(genDocumentId, genEmployee, genCustomer) { (documentId, employee, customer) =>
            val result = for {
              _ <- ElasticRequest.upsert[CustomerDocument](index, documentId, customer).execute
              _ <-
                ElasticRequest.upsert[EmployeeDocument](index, documentId, employee.copy(name = customer.name)).execute
              query = ElasticQuery.matches("name", customer.name)
              res  <- ElasticRequest.search[CustomerDocument](index, query).execute
            } yield res

            assertZIO(result.exit)(
              fails(
                isSubtype[Exception](
                  assertException("[Decoding Error] Could not parse all documents successfully: .address(missing))")
                )
              )
            )
          }
        }
      )
    ).provideShared(elasticsearchLayer) @@ nondeterministic
}
