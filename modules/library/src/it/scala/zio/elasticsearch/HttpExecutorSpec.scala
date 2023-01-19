package zio.elasticsearch

import zio.elasticsearch.CreationOutcome.{AlreadyExists, Created}
import zio.elasticsearch.DeletionOutcome.{Deleted, NotFound}
import zio.elasticsearch.ElasticQuery._
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
          assertZIO(ElasticRequest.createIndex(createIndexTestName, None).execute)(equalTo(Created))
        },
        test("return 'AlreadyExists' if index already exists") {
          val result = for {
            _   <- ElasticRequest.createIndex(createIndexTestName, None).execute
            res <- ElasticRequest.createIndex(createIndexTestName, None).execute
          } yield res

          assertZIO(result)(equalTo(AlreadyExists))
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
                _ <- ElasticRequest.deleteByQuery(index, matchAll()).execute
                _ <- ElasticRequest.upsert[CustomerDocument](index, firstDocumentId, firstCustomer).execute
                _ <-
                  ElasticRequest.upsert[CustomerDocument](index, secondDocumentId, secondCustomer).refreshTrue.execute
                query = range("balance").gte(100)
                res  <- ElasticRequest.search[CustomerDocument](index, query).execute
              } yield res

              assertZIO(result)(isNonEmpty)
          }
        },
        test("fail if any of results cannot be decoded") {
          checkOnce(genDocumentId, genDocumentId, genEmployee, genCustomer) {
            (employeeDocumentId, customerDocumentId, employee, customer) =>
              val result = for {
                _    <- ElasticRequest.deleteByQuery(index, matchAll()).execute
                _    <- ElasticRequest.upsert[CustomerDocument](index, customerDocumentId, customer).execute
                _    <- ElasticRequest.upsert[EmployeeDocument](index, employeeDocumentId, employee).refreshTrue.execute
                query = range("age").gte(0)
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
        ElasticRequest.deleteByQuery(index, matchAll()).refreshTrue.execute.orDie
      ),
      suite("deleting by query")(
        test("successfully deleted all matched documents") {
          checkOnce(genDocumentId, genCustomer, genDocumentId, genCustomer, genDocumentId, genCustomer) {
            (firstDocumentId, firstCustomer, secondDocumentId, secondCustomer, thirdDocumentId, thirdCustomer) =>
              val result =
                for {
                  _ <- ElasticRequest
                         .upsert[CustomerDocument](index, firstDocumentId, firstCustomer.copy(balance = 150))
                         .execute
                  _ <-
                    ElasticRequest
                      .upsert[CustomerDocument](index, secondDocumentId, secondCustomer.copy(balance = 350))
                      .execute
                  _ <-
                    ElasticRequest
                      .upsert[CustomerDocument](index, thirdDocumentId, thirdCustomer.copy(balance = 400))
                      .refreshTrue
                      .execute
                  deleteQuery = range("balance").gte(300)
                  _          <- ElasticRequest.deleteByQuery(index, deleteQuery).refreshTrue.execute
                  res        <- ElasticRequest.search[CustomerDocument](index, matchAll()).execute
                } yield res

              assertZIO(result)(hasSameElements(List(firstCustomer.copy(balance = 150))))
          }
        },
        test("returns Not Found when provided index is missing") {
          checkOnce(genIndexName) { missingIndex =>
            assertZIO(ElasticRequest.deleteByQuery(missingIndex, matchAll()).execute)(equalTo(NotFound))
          }
        }
      ),
      suite("bulk query")(
        test("successfully execute bulk query") {
          checkOnce(genDocumentId, genDocumentId, genDocumentId, genCustomer) {
            (firstDocId, secondDocId, thirdDocId, customer) =>
              val result =
                for {
                  _ <- ElasticRequest
                         .create[CustomerDocument](index, firstDocId, customer.copy(id = "randomIdString"))
                         .execute
                  _ <- ElasticRequest
                         .create[CustomerDocument](index, secondDocId, customer.copy(id = "randomIdString2"))
                         .refreshTrue
                         .execute
                  q1   = ElasticRequest.create[CustomerDocument](index, thirdDocId, customer)
                  q2   = ElasticRequest.create[CustomerDocument](index, customer.copy(id = "randomIdString3"))
                  q3   = ElasticRequest.upsert[CustomerDocument](index, firstDocId, customer.copy(balance = 3000))
                  q4   = ElasticRequest.deleteById(index, secondDocId)
                  res <- ElasticRequest.bulk(q1, q2, q3, q4).execute
                } yield res

              assertZIO(result)(equalTo(Created))
          }
        }
      )
    ).provideShared(elasticsearchLayer) @@ nondeterministic @@ sequential @@ prepareElasticsearchIndexForTests

}
