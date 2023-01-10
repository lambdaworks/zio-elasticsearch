package zio.elasticsearch

import zio._
import zio.elasticsearch.CreationOutcome.{AlreadyExists, Created}
import zio.elasticsearch.DeletionOutcome.{Deleted, NotFound}
import zio.test.Assertion._
import zio.test.TestAspect._
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
          checkOnce(genIndexName) { _ =>
            val result = for {
              res <- ElasticRequest.createIndex(createIndexTestName, None).execute
            } yield res
            assertZIO(result)(equalTo(Created))
          }
        },
        test("return 'AlreadyExists' if index already exists") {
          checkOnce(genIndexName) { _ =>
            val result = for {
              _   <- ElasticRequest.createIndex(createIndexTestName, None).execute
              res <- ElasticRequest.createIndex(createIndexTestName, None).execute
            } yield res

            assertZIO(result)(equalTo(AlreadyExists))
          }
        }
      ) @@ after(ElasticRequest.deleteIndex(createIndexTestName).execute.orDie),
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
          checkOnce(genDocumentId, genCustomer, genCustomer) { (documentId, firstCustomer, secondCustomer) =>
            val result = for {
              _   <- ElasticRequest.create[CustomerDocument](index, documentId, firstCustomer).execute
              _   <- ElasticRequest.upsert[CustomerDocument](index, documentId, secondCustomer).execute
              doc <- ElasticRequest.getById[CustomerDocument](index, documentId).execute
            } yield doc

            assertZIO(result)(isSome(equalTo(secondCustomer)))
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

            assertZIO(result.exit)(
              fails(isSubtype[Exception](assertException("Could not parse the document: .address(missing)")))
            )
          }
        }
      ),
      suite("searching documents")(
        test("search for document using range query") {
          checkOnce(genDocumentId, genCustomer, genDocumentId, genCustomer) {
            (firstDocumentId, firstCustomer, secondDocumentId, secondCustomer) =>
              val result = for {
                _ <- ElasticRequest.deleteByQuery(index, ElasticQuery.matchAll()).execute
                _ <- ElasticRequest.upsert[CustomerDocument](index, firstDocumentId, firstCustomer).refreshTrue.execute
                _ <-
                  ElasticRequest.upsert[CustomerDocument](index, secondDocumentId, secondCustomer).refreshTrue.execute
                query = ElasticQuery.range("balance").gte(100)
                res  <- ElasticRequest.search[CustomerDocument](index, query).execute
              } yield res

              assertZIO(result)(isNonEmpty)
          }
        },
        test("fail if any of results cannot be decoded") {
          checkOnce(genDocumentId, genDocumentId, genEmployee, genCustomer) {
            (employeeDocumentId, customerDocumentId, employee, customer) =>
              val result = for {
                _    <- ElasticRequest.deleteByQuery(index, ElasticQuery.matchAll()).execute
                _    <- ElasticRequest.upsert[CustomerDocument](index, customerDocumentId, customer).refreshTrue.execute
                _    <- ElasticRequest.upsert[EmployeeDocument](index, employeeDocumentId, employee).refreshTrue.execute
                query = ElasticQuery.range("age").gte(0)
                res  <- ElasticRequest.search[CustomerDocument](index, query).execute
              } yield res

              assertZIO(result.exit)(
                fails(
                  isSubtype[Exception](
                    assertException("Could not parse all documents successfully: .address(missing))")
                  )
                )
              )
          }
        }
      ) @@ shrinks(0) @@ sequential @@ afterAll(
        ElasticRequest.deleteByQuery(index, ElasticQuery.matchAll()).execute.orDie
      )
    ).provideShared(elasticsearchLayer) @@ nondeterministic @@ sequential @@ beforeAll(
      (for {
        _ <- ElasticRequest.createIndex(index, None).execute
        _ <- ElasticRequest.deleteByQuery(index, ElasticQuery.matchAll()).execute
        _ <- ZIO.succeed(Thread.sleep(3000))
      } yield ()).provide(elasticsearchLayer)
    )
}
