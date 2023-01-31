/*
 * Copyright 2022 LambdaWorks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package zio.elasticsearch

import zio.elasticsearch.CreationOutcome.{AlreadyExists, Created}
import zio.elasticsearch.DeletionOutcome.{Deleted, NotFound}
import zio.elasticsearch.ElasticQuery._
import zio.test.Assertion._
import zio.test.TestAspect._
import zio.test._

object HttpExecutorSpec extends IntegrationSpec {

  override def spec: Spec[TestEnvironment, Any] = {
    suite("Executor")(
      suite("HTTP Executor")(
        suite("creating document")(
          test("successfully create document") {
            checkOnce(genCustomer) { customer =>
              for {
                docId <- ElasticRequest.create[CustomerDocument](index, customer).execute
                res   <- ElasticRequest.getById[CustomerDocument](index, docId).execute
              } yield assert(res)(isSome(equalTo(customer)))
            }
          },
          test("successfully create document with ID given") {
            checkOnce(genDocumentId, genCustomer) { (documentId, customer) =>
              assertZIO(ElasticRequest.create[CustomerDocument](index, documentId, customer).execute)(equalTo(Created))
            }
          },
          test("return 'AlreadyExists' if document with given ID already exists") {
            checkOnce(genDocumentId, genCustomer, genCustomer) { (documentId, customer1, customer2) =>
              for {
                _   <- ElasticRequest.upsert[CustomerDocument](index, documentId, customer1).execute
                res <- ElasticRequest.create[CustomerDocument](index, documentId, customer2).execute
              } yield assert(res)(equalTo(AlreadyExists))
            }
          }
        ),
        suite("creating index")(
          test("successfully create index") {
            assertZIO(ElasticRequest.createIndex(createIndexTestName, None).execute)(equalTo(Created))
          },
          test("return 'AlreadyExists' if index already exists") {
            for {
              _   <- ElasticRequest.createIndex(createIndexTestName, None).execute
              res <- ElasticRequest.createIndex(createIndexTestName, None).execute
            } yield assert(res)(equalTo(AlreadyExists))
          }
        ) @@ after(ElasticRequest.deleteIndex(createIndexTestName).execute.orDie),
        suite("creating or updating document")(
          test("successfully create document") {
            checkOnce(genDocumentId, genCustomer) { (documentId, customer) =>
              for {
                _   <- ElasticRequest.upsert[CustomerDocument](index, documentId, customer).execute
                doc <- ElasticRequest.getById[CustomerDocument](index, documentId).execute
              } yield assert(doc)(isSome(equalTo(customer)))
            }
          },
          test("successfully update document") {
            checkOnce(genDocumentId, genCustomer, genCustomer) { (documentId, firstCustomer, secondCustomer) =>
              for {
                _   <- ElasticRequest.create[CustomerDocument](index, documentId, firstCustomer).execute
                _   <- ElasticRequest.upsert[CustomerDocument](index, documentId, secondCustomer).execute
                doc <- ElasticRequest.getById[CustomerDocument](index, documentId).execute
              } yield assert(doc)(isSome(equalTo(secondCustomer)))
            }
          }
        ),
        suite("deleting document by ID")(
          test("successfully delete existing document") {
            checkOnce(genDocumentId, genCustomer) { (documentId, customer) =>
              for {
                _   <- ElasticRequest.upsert[CustomerDocument](index, documentId, customer).execute
                res <- ElasticRequest.deleteById(index, documentId).execute
              } yield assert(res)(equalTo(Deleted))
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
              for {
                _   <- ElasticRequest.createIndex(name, None).execute
                res <- ElasticRequest.deleteIndex(name).execute
              } yield assert(res)(equalTo(Deleted))
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
              for {
                _   <- ElasticRequest.upsert[CustomerDocument](index, documentId, customer).execute
                res <- ElasticRequest.exists(index, documentId).execute
              } yield assert(res)(isTrue)
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
              for {
                _   <- ElasticRequest.upsert[CustomerDocument](index, documentId, customer).execute
                res <- ElasticRequest.getById[CustomerDocument](index, documentId).execute
              } yield assert(res)(isSome(equalTo(customer)))
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
                for {
                  _ <- ElasticRequest.deleteByQuery(firstSearchIndex, matchAll()).execute
                  _ <-
                    ElasticRequest.upsert[CustomerDocument](firstSearchIndex, firstDocumentId, firstCustomer).execute
                  _ <-
                    ElasticRequest
                      .upsert[CustomerDocument](firstSearchIndex, secondDocumentId, secondCustomer)
                      .refreshTrue
                      .execute
                  query = range("balance").gte(100)
                  res  <- ElasticRequest.search[CustomerDocument](firstSearchIndex, query).execute
                } yield assert(res)(isNonEmpty)
            }
          } @@ around(
            ElasticRequest.createIndex(firstSearchIndex, None).execute,
            ElasticRequest.deleteIndex(firstSearchIndex).execute.orDie
          ),
          test("fail if any of results cannot be decoded") {
            checkOnce(genDocumentId, genDocumentId, genEmployee, genCustomer) {
              (employeeDocumentId, customerDocumentId, employee, customer) =>
                val result =
                  for {
                    _ <- ElasticRequest.deleteByQuery(secondSearchIndex, matchAll()).execute
                    _ <-
                      ElasticRequest.upsert[CustomerDocument](secondSearchIndex, customerDocumentId, customer).execute
                    _ <- ElasticRequest
                           .upsert[EmployeeDocument](secondSearchIndex, employeeDocumentId, employee)
                           .refreshTrue
                           .execute
                    query = range("age").gte(0)
                    res  <- ElasticRequest.search[CustomerDocument](secondSearchIndex, query).execute
                  } yield res

                assertZIO(result.exit)(
                  fails(
                    isSubtype[Exception](
                      assertException("Could not parse all documents successfully: .address(missing))")
                    )
                  )
                )
            }
          } @@ around(
            ElasticRequest.createIndex(secondSearchIndex, None).execute,
            ElasticRequest.deleteIndex(secondSearchIndex).execute.orDie
          ),
          test("search for a document which contains a term using a wildcard query") {
            checkOnce(genDocumentId, genCustomer, genDocumentId, genCustomer) {
              (firstDocumentId, firstCustomer, secondDocumentId, secondCustomer) =>
                for {
                  _ <- ElasticRequest.deleteByQuery(firstSearchIndex, matchAll()).execute
                  _ <-
                    ElasticRequest.upsert[CustomerDocument](firstSearchIndex, firstDocumentId, firstCustomer).execute
                  _ <-
                    ElasticRequest
                      .upsert[CustomerDocument](firstSearchIndex, secondDocumentId, secondCustomer)
                      .refreshTrue
                      .execute
                  query = ElasticQuery.contains("name.keyword", firstCustomer.name.take(3))
                  res  <- ElasticRequest.search[CustomerDocument](firstSearchIndex, query).execute
                } yield assert(res)(Assertion.contains(firstCustomer))
            }
          } @@ around(
            ElasticRequest.createIndex(firstSearchIndex, None).execute,
            ElasticRequest.deleteIndex(firstSearchIndex).execute.orDie
          ),
          test("search for a document which starts with a term using a wildcard query") {
            checkOnce(genDocumentId, genCustomer, genDocumentId, genCustomer) {
              (firstDocumentId, firstCustomer, secondDocumentId, secondCustomer) =>
                for {
                  _ <- ElasticRequest.deleteByQuery(firstSearchIndex, matchAll()).execute
                  _ <-
                    ElasticRequest.upsert[CustomerDocument](firstSearchIndex, firstDocumentId, firstCustomer).execute
                  _ <-
                    ElasticRequest
                      .upsert[CustomerDocument](firstSearchIndex, secondDocumentId, secondCustomer)
                      .refreshTrue
                      .execute
                  query = ElasticQuery.startsWith("name.keyword", firstCustomer.name.take(3))
                  res  <- ElasticRequest.search[CustomerDocument](firstSearchIndex, query).execute
                } yield assert(res)(Assertion.contains(firstCustomer))
            }
          } @@ around(
            ElasticRequest.createIndex(firstSearchIndex, None).execute,
            ElasticRequest.deleteIndex(firstSearchIndex).execute.orDie
          ),
          test("search for a document which conforms to a pattern using a wildcard query") {
            checkOnce(genDocumentId, genCustomer, genDocumentId, genCustomer) {
              (firstDocumentId, firstCustomer, secondDocumentId, secondCustomer) =>
                for {
                  _ <- ElasticRequest.deleteByQuery(firstSearchIndex, matchAll()).execute
                  _ <-
                    ElasticRequest.upsert[CustomerDocument](firstSearchIndex, firstDocumentId, firstCustomer).execute
                  _ <-
                    ElasticRequest
                      .upsert[CustomerDocument](firstSearchIndex, secondDocumentId, secondCustomer)
                      .refreshTrue
                      .execute
                  query =
                    wildcard("name.keyword", s"${firstCustomer.name.take(2)}*${firstCustomer.name.takeRight(2)}")
                  res <- ElasticRequest.search[CustomerDocument](firstSearchIndex, query).execute
                } yield assert(res)(Assertion.contains(firstCustomer))
            }
          } @@ around(
            ElasticRequest.createIndex(firstSearchIndex, None).execute,
            ElasticRequest.deleteIndex(firstSearchIndex).execute.orDie
          )
        ) @@ shrinks(0),
        suite("deleting by query")(
          test("successfully delete all matched documents") {
            checkOnce(genDocumentId, genCustomer, genDocumentId, genCustomer, genDocumentId, genCustomer) {
              (firstDocumentId, firstCustomer, secondDocumentId, secondCustomer, thirdDocumentId, thirdCustomer) =>
                for {
                  _ <- ElasticRequest
                         .upsert[CustomerDocument](
                           deleteByQueryIndex,
                           firstDocumentId,
                           firstCustomer.copy(balance = 150)
                         )
                         .execute
                  _ <-
                    ElasticRequest
                      .upsert[CustomerDocument](
                        deleteByQueryIndex,
                        secondDocumentId,
                        secondCustomer.copy(balance = 350)
                      )
                      .execute
                  _ <-
                    ElasticRequest
                      .upsert[CustomerDocument](
                        deleteByQueryIndex,
                        thirdDocumentId,
                        thirdCustomer.copy(balance = 400)
                      )
                      .refreshTrue
                      .execute
                  deleteQuery = range("balance").gte(300)
                  _          <- ElasticRequest.deleteByQuery(deleteByQueryIndex, deleteQuery).refreshTrue.execute
                  res        <- ElasticRequest.search[CustomerDocument](deleteByQueryIndex, matchAll()).execute
                } yield assert(res)(hasSameElements(List(firstCustomer.copy(balance = 150))))
            }
          } @@ around(
            ElasticRequest.createIndex(deleteByQueryIndex, None).execute,
            ElasticRequest.deleteIndex(deleteByQueryIndex).execute.orDie
          ),
          test("returns NotFound when provided index is missing") {
            checkOnce(genIndexName) { missingIndex =>
              assertZIO(ElasticRequest.deleteByQuery(missingIndex, matchAll()).execute)(equalTo(NotFound))
            }
          }
        ),
        suite("bulk query")(
          test("successfully execute bulk query") {
            checkOnce(genDocumentId, genDocumentId, genDocumentId, genCustomer) {
              (firstDocId, secondDocId, thirdDocId, customer) =>
                for {
                  _ <- ElasticRequest
                         .create[CustomerDocument](index, firstDocId, customer.copy(id = "randomIdString"))
                         .execute
                  _ <- ElasticRequest
                         .create[CustomerDocument](index, secondDocId, customer.copy(id = "randomIdString2"))
                         .refreshTrue
                         .execute
                  req1 = ElasticRequest.create[CustomerDocument](index, thirdDocId, customer)
                  req2 = ElasticRequest.create[CustomerDocument](index, customer.copy(id = "randomIdString3"))
                  req3 = ElasticRequest.upsert[CustomerDocument](index, firstDocId, customer.copy(balance = 3000))
                  req4 = ElasticRequest.deleteById(index, secondDocId)
                  res <- ElasticRequest.bulk(req1, req2, req3, req4).execute
                } yield assert(res)(isUnit)
            }
          }
        )
      ) @@ nondeterministic @@ sequential @@ prepareElasticsearchIndexForTests @@ afterAll(
        ElasticRequest.deleteIndex(index).execute.orDie
      )
    ).provideShared(
      elasticsearchLayer
    )

  }

}
