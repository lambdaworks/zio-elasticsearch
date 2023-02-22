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

import zio.elasticsearch.ElasticQuery._
import zio.test.Assertion._
import zio.test.TestAspect._
import zio.test._

object HttpExecutorSpec extends IntegrationSpec {

  def spec: Spec[TestEnvironment, Any] = {
    suite("Executor")(
      suite("HTTP Executor")(
        suite("creating document")(
          test("successfully create document") {
            checkOnce(genCustomer) { customer =>
              for {
                docId <- Elasticsearch.execute(ElasticRequest.create[CustomerDocument](index, customer))
                res   <- Elasticsearch.execute(ElasticRequest.getById[CustomerDocument](index, docId))
              } yield assert(res)(isSome(equalTo(customer)))
            }
          },
          test("successfully create document with ID given") {
            checkOnce(genDocumentId, genCustomer) { (documentId, customer) =>
              assertZIO(Elasticsearch.execute(ElasticRequest.create[CustomerDocument](index, documentId, customer)))(
                equalTo(Created)
              )
            }
          },
          test("return 'AlreadyExists' if document with given ID already exists") {
            checkOnce(genDocumentId, genCustomer, genCustomer) { (documentId, customer1, customer2) =>
              for {
                _   <- Elasticsearch.execute(ElasticRequest.upsert[CustomerDocument](index, documentId, customer1))
                res <- Elasticsearch.execute(ElasticRequest.create[CustomerDocument](index, documentId, customer2))
              } yield assert(res)(equalTo(AlreadyExists))
            }
          }
        ),
        suite("creating index")(
          test("successfully create index") {
            assertZIO(Elasticsearch.execute(ElasticRequest.createIndex(createIndexTestName, None)))(equalTo(Created))
          },
          test("return 'AlreadyExists' if index already exists") {
            for {
              _   <- Elasticsearch.execute(ElasticRequest.createIndex(createIndexTestName, None))
              res <- Elasticsearch.execute(ElasticRequest.createIndex(createIndexTestName, None))
            } yield assert(res)(equalTo(AlreadyExists))
          }
        ) @@ after(Elasticsearch.execute(ElasticRequest.deleteIndex(createIndexTestName)).orDie),
        suite("creating or updating document")(
          test("successfully create document") {
            checkOnce(genDocumentId, genCustomer) { (documentId, customer) =>
              for {
                _   <- Elasticsearch.execute(ElasticRequest.upsert[CustomerDocument](index, documentId, customer))
                doc <- Elasticsearch.execute(ElasticRequest.getById[CustomerDocument](index, documentId))
              } yield assert(doc)(isSome(equalTo(customer)))
            }
          },
          test("successfully update document") {
            checkOnce(genDocumentId, genCustomer, genCustomer) { (documentId, firstCustomer, secondCustomer) =>
              for {
                _   <- Elasticsearch.execute(ElasticRequest.create[CustomerDocument](index, documentId, firstCustomer))
                _   <- Elasticsearch.execute(ElasticRequest.upsert[CustomerDocument](index, documentId, secondCustomer))
                doc <- Elasticsearch.execute(ElasticRequest.getById[CustomerDocument](index, documentId))
              } yield assert(doc)(isSome(equalTo(secondCustomer)))
            }
          }
        ),
        suite("deleting document by ID")(
          test("successfully delete existing document") {
            checkOnce(genDocumentId, genCustomer) { (documentId, customer) =>
              for {
                _   <- Elasticsearch.execute(ElasticRequest.upsert[CustomerDocument](index, documentId, customer))
                res <- Elasticsearch.execute(ElasticRequest.deleteById(index, documentId))
              } yield assert(res)(equalTo(Deleted))
            }
          },
          test("return 'NotFound' if the document does not exist") {
            checkOnce(genDocumentId) { documentId =>
              assertZIO(Elasticsearch.execute(ElasticRequest.deleteById(index, documentId)))(equalTo(NotFound))
            }
          }
        ),
        suite("delete index")(
          test("successfully delete existing index") {
            checkOnce(genIndexName) { name =>
              for {
                _   <- Elasticsearch.execute(ElasticRequest.createIndex(name, None))
                res <- Elasticsearch.execute(ElasticRequest.deleteIndex(name))
              } yield assert(res)(equalTo(Deleted))
            }
          },
          test("return 'NotFound' if index does not exists") {
            checkOnce(genIndexName) { name =>
              assertZIO(Elasticsearch.execute(ElasticRequest.deleteIndex(name)))(equalTo(NotFound))
            }
          }
        ),
        suite("finding document")(
          test("return true if the document exists") {
            checkOnce(genDocumentId, genCustomer) { (documentId, customer) =>
              for {
                _   <- Elasticsearch.execute(ElasticRequest.upsert[CustomerDocument](index, documentId, customer))
                res <- Elasticsearch.execute(ElasticRequest.exists(index, documentId))
              } yield assert(res)(isTrue)
            }
          },
          test("return false if the document does not exist") {
            checkOnce(genDocumentId) { documentId =>
              assertZIO(Elasticsearch.execute(ElasticRequest.exists(index, documentId)))(isFalse)
            }
          }
        ),
        suite("retrieving document by ID")(
          test("successfully return document") {
            checkOnce(genDocumentId, genCustomer) { (documentId, customer) =>
              for {
                _   <- Elasticsearch.execute(ElasticRequest.upsert[CustomerDocument](index, documentId, customer))
                res <- Elasticsearch.execute(ElasticRequest.getById[CustomerDocument](index, documentId))
              } yield assert(res)(isSome(equalTo(customer)))
            }
          },
          test("return None if the document does not exist") {
            checkOnce(genDocumentId) { documentId =>
              assertZIO(Elasticsearch.execute(ElasticRequest.getById[CustomerDocument](index, documentId)))(isNone)
            }
          },
          test("fail with throwable if decoding fails") {
            checkOnce(genDocumentId, genEmployee) { (documentId, employee) =>
              val result = for {
                _   <- Elasticsearch.execute(ElasticRequest.upsert[EmployeeDocument](index, documentId, employee))
                res <- Elasticsearch.execute(ElasticRequest.getById[CustomerDocument](index, documentId))
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
                  _ <- Elasticsearch.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                  _ <-
                    Elasticsearch.execute(
                      ElasticRequest.upsert[CustomerDocument](firstSearchIndex, firstDocumentId, firstCustomer)
                    )
                  _ <-
                    Elasticsearch.execute(
                      ElasticRequest
                        .upsert[CustomerDocument](firstSearchIndex, secondDocumentId, secondCustomer)
                        .refreshTrue
                    )
                  query = range("balance").gte(100)
                  res  <- Elasticsearch.execute(ElasticRequest.search[CustomerDocument](firstSearchIndex, query))
                } yield assert(res)(isNonEmpty)
            }
          } @@ around(
            Elasticsearch.execute(ElasticRequest.createIndex(firstSearchIndex, None)),
            Elasticsearch.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
          ),
          test("fail if any of results cannot be decoded") {
            checkOnce(genDocumentId, genDocumentId, genEmployee, genCustomer) {
              (employeeDocumentId, customerDocumentId, employee, customer) =>
                val result =
                  for {
                    _ <- Elasticsearch.execute(ElasticRequest.deleteByQuery(secondSearchIndex, matchAll))
                    _ <-
                      Elasticsearch.execute(
                        ElasticRequest.upsert[CustomerDocument](secondSearchIndex, customerDocumentId, customer)
                      )
                    _ <- Elasticsearch.execute(
                           ElasticRequest
                             .upsert[EmployeeDocument](secondSearchIndex, employeeDocumentId, employee)
                             .refreshTrue
                         )
                    query = range("age").gte(0)
                    res  <- Elasticsearch.execute(ElasticRequest.search[CustomerDocument](secondSearchIndex, query))
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
            Elasticsearch.execute(ElasticRequest.createIndex(secondSearchIndex, None)),
            Elasticsearch.execute(ElasticRequest.deleteIndex(secondSearchIndex)).orDie
          ),
          test("search for a document which contains a term using a wildcard query") {
            checkOnce(genDocumentId, genCustomer, genDocumentId, genCustomer) {
              (firstDocumentId, firstCustomer, secondDocumentId, secondCustomer) =>
                for {
                  _ <- Elasticsearch.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                  _ <-
                    Elasticsearch.execute(
                      ElasticRequest.upsert[CustomerDocument](firstSearchIndex, firstDocumentId, firstCustomer)
                    )
                  _ <-
                    Elasticsearch.execute(
                      ElasticRequest
                        .upsert[CustomerDocument](firstSearchIndex, secondDocumentId, secondCustomer)
                        .refreshTrue
                    )
                  query = ElasticQuery.contains("name.keyword", firstCustomer.name.take(3))
                  res  <- Elasticsearch.execute(ElasticRequest.search[CustomerDocument](firstSearchIndex, query))
                } yield assert(res)(Assertion.contains(firstCustomer))
            }
          } @@ around(
            Elasticsearch.execute(ElasticRequest.createIndex(firstSearchIndex, None)),
            Elasticsearch.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
          ),
          test("search for a document which starts with a term using a wildcard query") {
            checkOnce(genDocumentId, genCustomer, genDocumentId, genCustomer) {
              (firstDocumentId, firstCustomer, secondDocumentId, secondCustomer) =>
                for {
                  _ <- Elasticsearch.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                  _ <-
                    Elasticsearch.execute(
                      ElasticRequest.upsert[CustomerDocument](firstSearchIndex, firstDocumentId, firstCustomer)
                    )
                  _ <-
                    Elasticsearch.execute(
                      ElasticRequest
                        .upsert[CustomerDocument](firstSearchIndex, secondDocumentId, secondCustomer)
                        .refreshTrue
                    )
                  query = ElasticQuery.startsWith("name.keyword", firstCustomer.name.take(3))
                  res  <- Elasticsearch.execute(ElasticRequest.search[CustomerDocument](firstSearchIndex, query))
                } yield assert(res)(Assertion.contains(firstCustomer))
            }
          } @@ around(
            Elasticsearch.execute(ElasticRequest.createIndex(firstSearchIndex, None)),
            Elasticsearch.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
          ),
          test("search for a document which conforms to a pattern using a wildcard query") {
            checkOnce(genDocumentId, genCustomer, genDocumentId, genCustomer) {
              (firstDocumentId, firstCustomer, secondDocumentId, secondCustomer) =>
                for {
                  _ <- Elasticsearch.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                  _ <-
                    Elasticsearch.execute(
                      ElasticRequest.upsert[CustomerDocument](firstSearchIndex, firstDocumentId, firstCustomer)
                    )
                  _ <-
                    Elasticsearch.execute(
                      ElasticRequest
                        .upsert[CustomerDocument](firstSearchIndex, secondDocumentId, secondCustomer)
                        .refreshTrue
                    )
                  query =
                    wildcard("name.keyword", s"${firstCustomer.name.take(2)}*${firstCustomer.name.takeRight(2)}")
                  res <- Elasticsearch.execute(ElasticRequest.search[CustomerDocument](firstSearchIndex, query))
                } yield assert(res)(Assertion.contains(firstCustomer))
            }
          } @@ around(
            Elasticsearch.execute(ElasticRequest.createIndex(firstSearchIndex, None)),
            Elasticsearch.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
          )
        ) @@ shrinks(0),
        suite("deleting by query")(
          test("successfully delete all matched documents") {
            checkOnce(genDocumentId, genCustomer, genDocumentId, genCustomer, genDocumentId, genCustomer) {
              (firstDocumentId, firstCustomer, secondDocumentId, secondCustomer, thirdDocumentId, thirdCustomer) =>
                for {
                  _ <- Elasticsearch.execute(
                         ElasticRequest
                           .upsert[CustomerDocument](
                             deleteByQueryIndex,
                             firstDocumentId,
                             firstCustomer.copy(balance = 150)
                           )
                       )
                  _ <-
                    Elasticsearch.execute(
                      ElasticRequest
                        .upsert[CustomerDocument](
                          deleteByQueryIndex,
                          secondDocumentId,
                          secondCustomer.copy(balance = 350)
                        )
                    )
                  _ <-
                    Elasticsearch.execute(
                      ElasticRequest
                        .upsert[CustomerDocument](
                          deleteByQueryIndex,
                          thirdDocumentId,
                          thirdCustomer.copy(balance = 400)
                        )
                        .refreshTrue
                    )
                  deleteQuery = range("balance").gte(300)
                  _          <- Elasticsearch.execute(ElasticRequest.deleteByQuery(deleteByQueryIndex, deleteQuery).refreshTrue)
                  res        <- Elasticsearch.execute(ElasticRequest.search[CustomerDocument](deleteByQueryIndex, matchAll))
                } yield assert(res)(hasSameElements(List(firstCustomer.copy(balance = 150))))
            }
          } @@ around(
            Elasticsearch.execute(ElasticRequest.createIndex(deleteByQueryIndex, None)),
            Elasticsearch.execute(ElasticRequest.deleteIndex(deleteByQueryIndex)).orDie
          ),
          test("returns NotFound when provided index is missing") {
            checkOnce(genIndexName) { missingIndex =>
              assertZIO(Elasticsearch.execute(ElasticRequest.deleteByQuery(missingIndex, matchAll)))(equalTo(NotFound))
            }
          }
        ),
        suite("bulk query")(
          test("successfully execute bulk query") {
            checkOnce(genDocumentId, genDocumentId, genDocumentId, genCustomer) {
              (firstDocId, secondDocId, thirdDocId, customer) =>
                for {
                  _ <- Elasticsearch.execute(
                         ElasticRequest
                           .create[CustomerDocument](index, firstDocId, customer.copy(id = "randomIdString"))
                       )
                  _ <- Elasticsearch.execute(
                         ElasticRequest
                           .create[CustomerDocument](index, secondDocId, customer.copy(id = "randomIdString2"))
                           .refreshTrue
                       )
                  req1 = ElasticRequest.create[CustomerDocument](index, thirdDocId, customer)
                  req2 = ElasticRequest.create[CustomerDocument](index, customer.copy(id = "randomIdString3"))
                  req3 = ElasticRequest.upsert[CustomerDocument](index, firstDocId, customer.copy(balance = 3000))
                  req4 = ElasticRequest.deleteById(index, secondDocId)
                  res <- Elasticsearch.execute(ElasticRequest.bulk(req1, req2, req3, req4))
                } yield assert(res)(isUnit)
            }
          }
        )
      ) @@ nondeterministic @@ sequential @@ prepareElasticsearchIndexForTests @@ afterAll(
        Elasticsearch.execute(ElasticRequest.deleteIndex(index)).orDie
      )
    ).provideShared(
      elasticsearchLayer
    )
  }
}
