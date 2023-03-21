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

import zio.Chunk
import zio.elasticsearch.ElasticAggregation.{multipleAggregations, termsAggregation}
import zio.elasticsearch.ElasticQuery._
import zio.elasticsearch.SortMode.Max
import zio.elasticsearch.SortOrder._
import zio.elasticsearch.Sort.sortBy
import zio.stream.{Sink, ZSink}
import zio.test.Assertion._
import zio.test.TestAspect._
import zio.test._

import java.time.LocalDate
import scala.util.Random

object HttpExecutorSpec extends IntegrationSpec {

  def spec: Spec[TestEnvironment, Any] = {
    suite("Executor")(
      suite("HTTP Executor")(
        suite("aggregation")(
          test("aggregate using terms aggregation") {
            checkOnce(genDocumentId, genCustomer, genDocumentId, genCustomer) {
              (firstDocumentId, firstCustomer, secondDocumentId, secondCustomer) =>
                for {
                  _ <- ElasticExecutor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                  _ <- ElasticExecutor.execute(
                         ElasticRequest.upsert[CustomerDocument](firstSearchIndex, firstDocumentId, firstCustomer)
                       )
                  _ <- ElasticExecutor.execute(
                         ElasticRequest
                           .upsert[CustomerDocument](firstSearchIndex, secondDocumentId, secondCustomer)
                           .refreshTrue
                       )
                  aggregation = termsAggregation(name = "aggregationName", field = "name.keyword")
                  aggsRes <- ElasticExecutor
                               .execute(
                                 ElasticRequest
                                   .aggregate(index = firstSearchIndex, aggregation = aggregation)
                               )
                               .aggregations
                } yield assert(aggsRes)(isNonEmpty)
            }
          } @@ around(
            ElasticExecutor.execute(ElasticRequest.createIndex(firstSearchIndex)),
            ElasticExecutor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
          ),
          test("aggregate using multiple terms aggregations") {
            checkOnce(genDocumentId, genCustomer, genDocumentId, genCustomer) {
              (firstDocumentId, firstCustomer, secondDocumentId, secondCustomer) =>
                for {
                  _ <- ElasticExecutor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                  _ <- ElasticExecutor.execute(
                         ElasticRequest.upsert[CustomerDocument](firstSearchIndex, firstDocumentId, firstCustomer)
                       )
                  _ <- ElasticExecutor.execute(
                         ElasticRequest
                           .upsert[CustomerDocument](firstSearchIndex, secondDocumentId, secondCustomer)
                           .refreshTrue
                       )
                  aggregation = multipleAggregations.aggregations(
                                  termsAggregation(name = "aggregationName", field = "name.keyword"),
                                  termsAggregation(name = "aggregationAge", field = "age.keyword")
                                )
                  aggsRes <- ElasticExecutor
                               .execute(
                                 ElasticRequest
                                   .aggregate(index = firstSearchIndex, aggregation = aggregation)
                               )
                               .aggregations
                } yield assert(aggsRes)(isNonEmpty)
            }
          } @@ around(
            ElasticExecutor.execute(ElasticRequest.createIndex(firstSearchIndex)),
            ElasticExecutor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
          ),
          test("aggregate using terms aggregation with nested terms aggregation") {
            checkOnce(genDocumentId, genCustomer, genDocumentId, genCustomer) {
              (firstDocumentId, firstCustomer, secondDocumentId, secondCustomer) =>
                for {
                  _ <- ElasticExecutor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                  _ <- ElasticExecutor.execute(
                         ElasticRequest.upsert[CustomerDocument](firstSearchIndex, firstDocumentId, firstCustomer)
                       )
                  _ <- ElasticExecutor.execute(
                         ElasticRequest
                           .upsert[CustomerDocument](firstSearchIndex, secondDocumentId, secondCustomer)
                           .refreshTrue
                       )
                  aggregation = termsAggregation(name = "aggregationName", field = "name.keyword")
                                  .withSubAgg(termsAggregation(name = "aggregationAge", field = "age.keyword"))
                  aggsRes <- ElasticExecutor
                               .execute(
                                 ElasticRequest
                                   .aggregate(index = firstSearchIndex, aggregation = aggregation)
                               )
                               .aggregations
                } yield assert(aggsRes)(isNonEmpty)
            }
          } @@ around(
            ElasticExecutor.execute(ElasticRequest.createIndex(firstSearchIndex)),
            ElasticExecutor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
          )
        ),
        suite("search with aggregation")(
          test("search using match all query with multiple terms aggregations") {
            checkOnce(genDocumentId, genCustomer, genDocumentId, genCustomer) {
              (firstDocumentId, firstCustomer, secondDocumentId, secondCustomer) =>
                for {
                  _ <- ElasticExecutor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                  _ <- ElasticExecutor.execute(
                         ElasticRequest.upsert[CustomerDocument](firstSearchIndex, firstDocumentId, firstCustomer)
                       )
                  _ <- ElasticExecutor.execute(
                         ElasticRequest
                           .upsert[CustomerDocument](firstSearchIndex, secondDocumentId, secondCustomer)
                           .refreshTrue
                       )
                  query = matchAll
                  aggregation =
                    termsAggregation("aggregationName", "name.keyword").withAgg(
                      termsAggregation("aggregationAge", "age.keyword")
                    )
                  res <- ElasticExecutor.execute(
                           ElasticRequest
                             .searchWithAggregation(
                               index = firstSearchIndex,
                               query = query,
                               aggregation = aggregation
                             )
                         )
                  docs <- res.documentAs[CustomerDocument]
                  aggs <- res.aggregations
                } yield assert(docs)(isNonEmpty) && assert(aggs)(
                  isNonEmpty
                )
            }
          } @@ around(
            ElasticExecutor.execute(ElasticRequest.createIndex(firstSearchIndex)),
            ElasticExecutor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
          ),
          test("search using match all query with multiple terms aggregations with descending sort on one field") {
            checkOnce(genDocumentId, genEmployee, genDocumentId, genEmployee) {
              (firstDocumentId, firstEmployee, secondDocumentId, secondEmployee) =>
                val firstEmployeeWithFixedAge  = firstEmployee.copy(age = 25)
                val secondEmployeeWithFixedAge = secondEmployee.copy(age = 32)
                for {
                  _ <- ElasticExecutor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                  _ <- ElasticExecutor.execute(
                         ElasticRequest
                           .upsert[EmployeeDocument](firstSearchIndex, firstDocumentId, firstEmployeeWithFixedAge)
                       )
                  _ <- ElasticExecutor.execute(
                         ElasticRequest
                           .upsert[EmployeeDocument](firstSearchIndex, secondDocumentId, secondEmployeeWithFixedAge)
                           .refreshTrue
                       )
                  query = matchAll
                  aggregation =
                    termsAggregation("aggregationName", "name.keyword").withAgg(
                      termsAggregation("aggregationAge", "age.keyword")
                    )
                  res <- ElasticExecutor.execute(
                           ElasticRequest
                             .searchWithAggregation(
                               index = firstSearchIndex,
                               query = query,
                               aggregation = aggregation
                             )
                             .sortBy(sortBy("age").order(Desc))
                         )
                  docs <- res.documentAs[EmployeeDocument]
                  aggs <- res.aggregations
                } yield assert(docs)(equalTo(List(secondEmployeeWithFixedAge, firstEmployeeWithFixedAge))) && assert(
                  aggs
                )(
                  isNonEmpty
                )
            }
          } @@ around(
            ElasticExecutor.execute(ElasticRequest.createIndex(firstSearchIndex)),
            ElasticExecutor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
          ),
          test("search using match all query with terms aggregations with nested terms aggregation") {
            checkOnce(genDocumentId, genCustomer, genDocumentId, genCustomer) {
              (firstDocumentId, firstCustomer, secondDocumentId, secondCustomer) =>
                for {
                  _ <- ElasticExecutor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                  _ <- ElasticExecutor.execute(
                         ElasticRequest.upsert[CustomerDocument](firstSearchIndex, firstDocumentId, firstCustomer)
                       )
                  _ <- ElasticExecutor.execute(
                         ElasticRequest
                           .upsert[CustomerDocument](firstSearchIndex, secondDocumentId, secondCustomer)
                           .refreshTrue
                       )
                  query = matchAll
                  aggregation =
                    termsAggregation("aggregationName", "name.keyword").withSubAgg(
                      termsAggregation("aggregationAge", "age.keyword")
                    )
                  res <- ElasticExecutor.execute(
                           ElasticRequest
                             .searchWithAggregation(
                               index = firstSearchIndex,
                               query = query,
                               aggregation = aggregation
                             )
                         )
                  docs <- res.documentAs[CustomerDocument]
                  aggs <- res.aggregations
                } yield assert(docs)(isNonEmpty) && assert(aggs)(
                  isNonEmpty
                )
            }
          } @@ around(
            ElasticExecutor.execute(ElasticRequest.createIndex(firstSearchIndex)),
            ElasticExecutor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
          )
        ),
        suite("counting documents")(
          test("successfully count documents with given query") {
            checkOnce(genCustomer) { customer =>
              for {
                _ <- ElasticExecutor.execute(ElasticRequest.deleteByQuery(firstCountIndex, matchAll))
                _ <- ElasticExecutor.execute(
                       ElasticRequest.create[CustomerDocument](firstCountIndex, customer).refreshTrue
                     )
                res <- ElasticExecutor.execute(ElasticRequest.count(firstCountIndex, matchAll))
              } yield assert(res)(equalTo(1))
            }
          } @@ around(
            ElasticExecutor.execute(ElasticRequest.createIndex(firstCountIndex)),
            ElasticExecutor.execute(ElasticRequest.deleteIndex(firstCountIndex)).orDie
          ),
          test("successfully count documents without given query") {
            checkOnce(genCustomer) { customer =>
              for {
                _ <- ElasticExecutor.execute(ElasticRequest.deleteByQuery(secondCountIndex, matchAll))
                _ <- ElasticExecutor.execute(
                       ElasticRequest.create[CustomerDocument](secondCountIndex, customer).refreshTrue
                     )
                res <- ElasticExecutor.execute(ElasticRequest.count(secondCountIndex))
              } yield assert(res)(equalTo(1))
            }
          } @@ around(
            ElasticExecutor.execute(ElasticRequest.createIndex(secondCountIndex)),
            ElasticExecutor.execute(ElasticRequest.deleteIndex(secondCountIndex)).orDie
          )
        ) @@ shrinks(0),
        suite("creating document")(
          test("successfully create document") {
            checkOnce(genCustomer) { customer =>
              for {
                docId <- ElasticExecutor.execute(ElasticRequest.create[CustomerDocument](index, customer))
                res   <- ElasticExecutor.execute(ElasticRequest.getById(index, docId)).documentAs[CustomerDocument]
              } yield assert(res)(isSome(equalTo(customer)))
            }
          },
          test("successfully create document with ID given") {
            checkOnce(genDocumentId, genCustomer) { (documentId, customer) =>
              assertZIO(ElasticExecutor.execute(ElasticRequest.create[CustomerDocument](index, documentId, customer)))(
                equalTo(Created)
              )
            }
          },
          test("return 'AlreadyExists' if document with given ID already exists") {
            checkOnce(genDocumentId, genCustomer, genCustomer) { (documentId, customer1, customer2) =>
              for {
                _   <- ElasticExecutor.execute(ElasticRequest.upsert[CustomerDocument](index, documentId, customer1))
                res <- ElasticExecutor.execute(ElasticRequest.create[CustomerDocument](index, documentId, customer2))
              } yield assert(res)(equalTo(AlreadyExists))
            }
          }
        ),
        suite("creating index")(
          test("successfully create index") {
            assertZIO(ElasticExecutor.execute(ElasticRequest.createIndex(createIndexTestName)))(equalTo(Created))
          },
          test("return 'AlreadyExists' if index already exists") {
            for {
              _   <- ElasticExecutor.execute(ElasticRequest.createIndex(createIndexTestName))
              res <- ElasticExecutor.execute(ElasticRequest.createIndex(createIndexTestName))
            } yield assert(res)(equalTo(AlreadyExists))
          }
        ) @@ after(ElasticExecutor.execute(ElasticRequest.deleteIndex(createIndexTestName)).orDie),
        suite("creating or updating document")(
          test("successfully create document") {
            checkOnce(genDocumentId, genCustomer) { (documentId, customer) =>
              for {
                _   <- ElasticExecutor.execute(ElasticRequest.upsert[CustomerDocument](index, documentId, customer))
                doc <- ElasticExecutor.execute(ElasticRequest.getById(index, documentId)).documentAs[CustomerDocument]
              } yield assert(doc)(isSome(equalTo(customer)))
            }
          },
          test("successfully update document") {
            checkOnce(genDocumentId, genCustomer, genCustomer) { (documentId, firstCustomer, secondCustomer) =>
              for {
                _   <- ElasticExecutor.execute(ElasticRequest.create[CustomerDocument](index, documentId, firstCustomer))
                _   <- ElasticExecutor.execute(ElasticRequest.upsert[CustomerDocument](index, documentId, secondCustomer))
                doc <- ElasticExecutor.execute(ElasticRequest.getById(index, documentId)).documentAs[CustomerDocument]
              } yield assert(doc)(isSome(equalTo(secondCustomer)))
            }
          }
        ),
        suite("deleting document by ID")(
          test("successfully delete existing document") {
            checkOnce(genDocumentId, genCustomer) { (documentId, customer) =>
              for {
                _   <- ElasticExecutor.execute(ElasticRequest.upsert[CustomerDocument](index, documentId, customer))
                res <- ElasticExecutor.execute(ElasticRequest.deleteById(index, documentId))
              } yield assert(res)(equalTo(Deleted))
            }
          },
          test("return 'NotFound' if the document does not exist") {
            checkOnce(genDocumentId) { documentId =>
              assertZIO(ElasticExecutor.execute(ElasticRequest.deleteById(index, documentId)))(equalTo(NotFound))
            }
          }
        ),
        suite("delete index")(
          test("successfully delete existing index") {
            checkOnce(genIndexName) { name =>
              for {
                _   <- ElasticExecutor.execute(ElasticRequest.createIndex(name))
                res <- ElasticExecutor.execute(ElasticRequest.deleteIndex(name))
              } yield assert(res)(equalTo(Deleted))
            }
          },
          test("return 'NotFound' if index does not exists") {
            checkOnce(genIndexName) { name =>
              assertZIO(ElasticExecutor.execute(ElasticRequest.deleteIndex(name)))(equalTo(NotFound))
            }
          }
        ),
        suite("finding document")(
          test("return true if the document exists") {
            checkOnce(genDocumentId, genCustomer) { (documentId, customer) =>
              for {
                _   <- ElasticExecutor.execute(ElasticRequest.upsert[CustomerDocument](index, documentId, customer))
                res <- ElasticExecutor.execute(ElasticRequest.exists(index, documentId))
              } yield assert(res)(isTrue)
            }
          },
          test("return false if the document does not exist") {
            checkOnce(genDocumentId) { documentId =>
              assertZIO(ElasticExecutor.execute(ElasticRequest.exists(index, documentId)))(isFalse)
            }
          }
        ),
        suite("retrieving document by ID")(
          test("successfully return document") {
            checkOnce(genDocumentId, genCustomer) { (documentId, customer) =>
              for {
                _   <- ElasticExecutor.execute(ElasticRequest.upsert[CustomerDocument](index, documentId, customer))
                res <- ElasticExecutor.execute(ElasticRequest.getById(index, documentId)).documentAs[CustomerDocument]
              } yield assert(res)(isSome(equalTo(customer)))
            }
          },
          test("return None if the document does not exist") {
            checkOnce(genDocumentId) { documentId =>
              assertZIO(
                ElasticExecutor.execute(ElasticRequest.getById(index, documentId)).documentAs[CustomerDocument]
              )(isNone)
            }
          },
          test("fail with throwable if decoding fails") {
            checkOnce(genDocumentId, genEmployee) { (documentId, employee) =>
              val result = for {
                _   <- ElasticExecutor.execute(ElasticRequest.upsert[EmployeeDocument](index, documentId, employee))
                res <- ElasticExecutor.execute(ElasticRequest.getById(index, documentId)).documentAs[CustomerDocument]
              } yield res

              assertZIO(result.exit)(
                fails(isSubtype[Exception](assertException("Could not parse the document: .address(missing)")))
              )
            }
          }
        ),
        suite("searching for documents")(
          test("search for document using range query") {
            checkOnce(genDocumentId, genCustomer, genDocumentId, genCustomer) {
              (firstDocumentId, firstCustomer, secondDocumentId, secondCustomer) =>
                for {
                  _ <- ElasticExecutor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                  _ <- ElasticExecutor.execute(
                         ElasticRequest.upsert[CustomerDocument](firstSearchIndex, firstDocumentId, firstCustomer)
                       )
                  _ <- ElasticExecutor.execute(
                         ElasticRequest
                           .upsert[CustomerDocument](firstSearchIndex, secondDocumentId, secondCustomer)
                           .refreshTrue
                       )
                  query = range("balance").gte(100)
                  res <-
                    ElasticExecutor.execute(ElasticRequest.search(firstSearchIndex, query)).documentAs[CustomerDocument]
                } yield assert(res)(isNonEmpty)
            }
          } @@ around(
            ElasticExecutor.execute(ElasticRequest.createIndex(firstSearchIndex)),
            ElasticExecutor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
          ),
          test("fail if any of results cannot be decoded") {
            checkOnce(genDocumentId, genDocumentId, genEmployee, genCustomer) {
              (employeeDocumentId, customerDocumentId, employee, customer) =>
                val result =
                  for {
                    _ <- ElasticExecutor.execute(ElasticRequest.deleteByQuery(secondSearchIndex, matchAll))
                    _ <- ElasticExecutor.execute(
                           ElasticRequest.upsert[CustomerDocument](secondSearchIndex, customerDocumentId, customer)
                         )
                    _ <- ElasticExecutor.execute(
                           ElasticRequest
                             .upsert[EmployeeDocument](secondSearchIndex, employeeDocumentId, employee)
                             .refreshTrue
                         )
                    query = range("age").gte(0)
                    res <- ElasticExecutor
                             .execute(ElasticRequest.search(secondSearchIndex, query))
                             .documentAs[CustomerDocument]
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
            ElasticExecutor.execute(ElasticRequest.createIndex(secondSearchIndex)),
            ElasticExecutor.execute(ElasticRequest.deleteIndex(secondSearchIndex)).orDie
          ),
          test("search for a document which contains a term using a wildcard query") {
            checkOnce(genDocumentId, genCustomer, genDocumentId, genCustomer) {
              (firstDocumentId, firstCustomer, secondDocumentId, secondCustomer) =>
                for {
                  _ <- ElasticExecutor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                  _ <- ElasticExecutor.execute(
                         ElasticRequest.upsert[CustomerDocument](firstSearchIndex, firstDocumentId, firstCustomer)
                       )
                  _ <- ElasticExecutor.execute(
                         ElasticRequest
                           .upsert[CustomerDocument](firstSearchIndex, secondDocumentId, secondCustomer)
                           .refreshTrue
                       )
                  query = ElasticQuery.contains("name.keyword", firstCustomer.name.take(3))
                  res <- ElasticExecutor
                           .execute(ElasticRequest.search(firstSearchIndex, query))
                           .documentAs[CustomerDocument]
                } yield assert(res)(Assertion.contains(firstCustomer))
            }
          } @@ around(
            ElasticExecutor.execute(ElasticRequest.createIndex(firstSearchIndex)),
            ElasticExecutor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
          ),
          test("search for a document which starts with a term using a wildcard query") {
            checkOnce(genDocumentId, genCustomer, genDocumentId, genCustomer) {
              (firstDocumentId, firstCustomer, secondDocumentId, secondCustomer) =>
                for {
                  _ <- ElasticExecutor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                  _ <- ElasticExecutor.execute(
                         ElasticRequest.upsert[CustomerDocument](firstSearchIndex, firstDocumentId, firstCustomer)
                       )
                  _ <- ElasticExecutor.execute(
                         ElasticRequest
                           .upsert[CustomerDocument](firstSearchIndex, secondDocumentId, secondCustomer)
                           .refreshTrue
                       )
                  query = ElasticQuery.startsWith("name.keyword", firstCustomer.name.take(3))
                  res <- ElasticExecutor
                           .execute(ElasticRequest.search(firstSearchIndex, query))
                           .documentAs[CustomerDocument]
                } yield assert(res)(Assertion.contains(firstCustomer))
            }
          } @@ around(
            ElasticExecutor.execute(ElasticRequest.createIndex(firstSearchIndex)),
            ElasticExecutor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
          ),
          test("search for a document which conforms to a pattern using a wildcard query") {
            checkOnce(genDocumentId, genCustomer, genDocumentId, genCustomer) {
              (firstDocumentId, firstCustomer, secondDocumentId, secondCustomer) =>
                for {
                  _ <- ElasticExecutor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                  _ <-
                    ElasticExecutor.execute(
                      ElasticRequest.upsert[CustomerDocument](firstSearchIndex, firstDocumentId, firstCustomer)
                    )
                  _ <- ElasticExecutor.execute(
                         ElasticRequest
                           .upsert[CustomerDocument](firstSearchIndex, secondDocumentId, secondCustomer)
                           .refreshTrue
                       )
                  query =
                    wildcard("name.keyword", s"${firstCustomer.name.take(2)}*${firstCustomer.name.takeRight(2)}")
                  res <-
                    ElasticExecutor.execute(ElasticRequest.search(firstSearchIndex, query)).documentAs[CustomerDocument]
                } yield assert(res)(Assertion.contains(firstCustomer))
            }
          } @@ around(
            ElasticExecutor.execute(ElasticRequest.createIndex(firstSearchIndex)),
            ElasticExecutor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
          )
        ) @@ shrinks(0),
        suite("searching for sorted documents")(
          test("search for document sorted by descending age and by descending birthDate using range query") {
            checkOnce(genDocumentId, genEmployee, genDocumentId, genEmployee) {
              (firstDocumentId, firstEmployee, secondDocumentId, secondEmployee) =>
                val firstCustomerWithFixedAge = firstEmployee.copy(age = 30, birthDate = LocalDate.parse("1993-12-05"))
                val secondCustomerWithFixedAge =
                  secondEmployee.copy(age = 36, birthDate = LocalDate.parse("1987-12-05"))
                for {
                  _ <- ElasticExecutor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                  _ <- ElasticExecutor.execute(
                         ElasticRequest
                           .upsert[EmployeeDocument](firstSearchIndex, firstDocumentId, firstCustomerWithFixedAge)
                       )
                  _ <- ElasticExecutor.execute(
                         ElasticRequest
                           .upsert[EmployeeDocument](
                             firstSearchIndex,
                             secondDocumentId,
                             secondCustomerWithFixedAge
                           )
                           .refreshTrue
                       )
                  query = range("age").gte(20)
                  res <- ElasticExecutor
                           .execute(
                             ElasticRequest
                               .search(firstSearchIndex, query)
                               .sortBy(
                                 sortBy("age").order(Desc),
                                 sortBy("birthDate").order(Desc).format("strict_date_optional_time_nanos")
                               )
                           )
                           .documentAs[EmployeeDocument]
                } yield assert(res)(
                  equalTo(List(secondCustomerWithFixedAge, firstCustomerWithFixedAge))
                )
            }
          } @@ around(
            ElasticExecutor.execute(ElasticRequest.createIndex(firstSearchIndex)),
            ElasticExecutor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
          ),
          test("search for document sorted by ascending age and by ascending birthDate using range query") {
            checkOnce(genDocumentId, genEmployee, genDocumentId, genEmployee) {
              (firstDocumentId, firstEmployee, secondDocumentId, secondEmployee) =>
                val firstCustomerWithFixedAge = firstEmployee.copy(age = 30, birthDate = LocalDate.parse("1993-12-05"))
                val secondCustomerWithFixedAge =
                  secondEmployee.copy(age = 36, birthDate = LocalDate.parse("1987-12-05"))
                for {
                  _ <- ElasticExecutor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                  _ <- ElasticExecutor.execute(
                         ElasticRequest
                           .upsert[EmployeeDocument](firstSearchIndex, firstDocumentId, firstCustomerWithFixedAge)
                       )
                  _ <- ElasticExecutor.execute(
                         ElasticRequest
                           .upsert[EmployeeDocument](
                             firstSearchIndex,
                             secondDocumentId,
                             secondCustomerWithFixedAge
                           )
                           .refreshTrue
                       )
                  query = range("age").gte(20)
                  res <- ElasticExecutor
                           .execute(
                             ElasticRequest
                               .search(firstSearchIndex, query)
                               .sortBy(
                                 sortBy("age").order(Asc),
                                 sortBy("birthDate").order(Asc).format("strict_date_optional_time_nanos")
                               )
                           )
                           .documentAs[EmployeeDocument]
                } yield assert(res)(
                  equalTo(List(firstCustomerWithFixedAge, secondCustomerWithFixedAge))
                )
            }
          } @@ around(
            ElasticExecutor.execute(ElasticRequest.createIndex(firstSearchIndex)),
            ElasticExecutor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
          ),
          test("search for document sorted descending with 'max' mode by one field using matchAll query") {
            checkOnce(genDocumentId, genEmployee, genDocumentId, genEmployee) {
              (firstDocumentId, firstCustomer, secondDocumentId, secondCustomer) =>
                val firstEmployeeWithFixedSectors  = firstCustomer.copy(sectorsIds = List(11, 4, 37))
                val secondEmployeeWithFixedSectors = secondCustomer.copy(sectorsIds = List(30, 29, 35))
                for {
                  _ <- ElasticExecutor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                  _ <- ElasticExecutor.execute(
                         ElasticRequest
                           .upsert[EmployeeDocument](firstSearchIndex, firstDocumentId, firstEmployeeWithFixedSectors)
                       )
                  _ <- ElasticExecutor.execute(
                         ElasticRequest
                           .upsert[EmployeeDocument](
                             firstSearchIndex,
                             secondDocumentId,
                             secondEmployeeWithFixedSectors
                           )
                           .refreshTrue
                       )
                  query = matchAll
                  res <- ElasticExecutor
                           .execute(
                             ElasticRequest
                               .search(firstSearchIndex, query)
                               .sortBy(sortBy("sectorsIds").mode(Max).order(Desc))
                           )
                           .documentAs[EmployeeDocument]
                } yield assert(res)(
                  equalTo(List(firstEmployeeWithFixedSectors, secondEmployeeWithFixedSectors))
                )
            }
          } @@ around(
            ElasticExecutor.execute(ElasticRequest.createIndex(firstSearchIndex)),
            ElasticExecutor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
          )
        ) @@ shrinks(0),
        suite("searching for documents using scroll API and returning them as a stream")(
          test("search for documents using range query") {
            checkOnce(genDocumentId, genCustomer, genDocumentId, genCustomer) {
              (firstDocumentId, firstCustomer, secondDocumentId, secondCustomer) =>
                val sink: Sink[Throwable, Item, Nothing, Chunk[Item]] = ZSink.collectAll[Item]

                for {
                  _ <- ElasticExecutor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                  _ <- ElasticExecutor.execute(
                         ElasticRequest.upsert[CustomerDocument](firstSearchIndex, firstDocumentId, firstCustomer)
                       )
                  _ <- ElasticExecutor.execute(
                         ElasticRequest
                           .upsert[CustomerDocument](firstSearchIndex, secondDocumentId, secondCustomer)
                           .refreshTrue
                       )
                  query = range("balance").gte(100)
                  res  <- ElasticExecutor.stream(ElasticRequest.search(firstSearchIndex, query)).run(sink)
                } yield assert(res)(isNonEmpty)
            }
          } @@ around(
            ElasticExecutor.execute(ElasticRequest.createIndex(firstSearchIndex)),
            ElasticExecutor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
          ),
          test("search for documents using range query with multiple pages") {
            checkOnce(genCustomer) { customer =>
              def sink: Sink[Throwable, Item, Nothing, Chunk[Item]] = ZSink.collectAll[Item]

              for {
                _ <- ElasticExecutor.execute(ElasticRequest.deleteByQuery(secondSearchIndex, matchAll))
                reqs = (0 to 203).map { _ =>
                         ElasticRequest.create[CustomerDocument](
                           secondSearchIndex,
                           customer.copy(id = Random.alphanumeric.take(5).mkString, balance = 150)
                         )
                       }
                _    <- ElasticExecutor.execute(ElasticRequest.bulk(reqs: _*).refreshTrue)
                query = range("balance").gte(100)
                res <- ElasticExecutor
                         .stream(
                           ElasticRequest.search(secondSearchIndex, query)
                         )
                         .run(sink)
              } yield assert(res)(hasSize(equalTo(204)))
            }
          } @@ around(
            ElasticExecutor.execute(ElasticRequest.createIndex(secondSearchIndex)),
            ElasticExecutor.execute(ElasticRequest.deleteIndex(secondSearchIndex)).orDie
          ),
          test("search for documents using range query with multiple pages and return type") {
            checkOnce(genCustomer) { customer =>
              def sink: Sink[Throwable, CustomerDocument, Nothing, Chunk[CustomerDocument]] =
                ZSink.collectAll[CustomerDocument]

              for {
                _ <- ElasticExecutor.execute(ElasticRequest.deleteByQuery(secondSearchIndex, matchAll))
                reqs = (0 to 200).map { _ =>
                         ElasticRequest.create[CustomerDocument](
                           secondSearchIndex,
                           customer.copy(id = Random.alphanumeric.take(5).mkString, balance = 150)
                         )
                       }
                _    <- ElasticExecutor.execute(ElasticRequest.bulk(reqs: _*).refreshTrue)
                query = range("balance").gte(100)
                res <- ElasticExecutor
                         .streamAs[CustomerDocument](ElasticRequest.search(secondSearchIndex, query))
                         .run(sink)
              } yield assert(res)(hasSize(equalTo(201)))
            }
          } @@ around(
            ElasticExecutor.execute(ElasticRequest.createIndex(secondSearchIndex)),
            ElasticExecutor.execute(ElasticRequest.deleteIndex(secondSearchIndex)).orDie
          ),
          test("search for documents using range query - empty stream") {
            val sink: Sink[Throwable, Item, Nothing, Chunk[Item]] = ZSink.collectAll[Item]

            for {
              _    <- ElasticExecutor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
              query = range("balance").gte(100)
              res  <- ElasticExecutor.stream(ElasticRequest.search(firstSearchIndex, query)).run(sink)
            } yield assert(res)(hasSize(equalTo(0)))
          } @@ around(
            ElasticExecutor.execute(ElasticRequest.createIndex(firstSearchIndex)),
            ElasticExecutor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
          )
        ) @@ shrinks(0),
        suite("searching for documents using PIT (point in time) and returning them as a stream")(
          test("successfully create PIT and return stream results") {
            checkOnce(genCustomer) { customer =>
              def sink: Sink[Throwable, Item, Nothing, Chunk[Item]] =
                ZSink.collectAll[Item]

              for {
                _ <- ElasticExecutor.execute(ElasticRequest.deleteByQuery(secondSearchIndex, matchAll))
                reqs = (0 to 200).map { _ =>
                         ElasticRequest.create[CustomerDocument](
                           secondSearchIndex,
                           customer.copy(id = Random.alphanumeric.take(5).mkString, balance = 150)
                         )
                       }
                _    <- ElasticExecutor.execute(ElasticRequest.bulk(reqs: _*).refreshTrue)
                query = range("balance").gte(100)
                res <- ElasticExecutor
                         .stream(ElasticRequest.search(secondSearchIndex, query), StreamConfig.searchAfter)
                         .run(sink)
              } yield assert(res)(hasSize(equalTo(201)))
            }
          } @@ around(
            ElasticExecutor.execute(ElasticRequest.createIndex(secondSearchIndex)),
            ElasticExecutor.execute(ElasticRequest.deleteIndex(secondSearchIndex)).orDie
          ),
          test(
            "successfully create PIT and return stream results with changed page size and different keep alive parameters"
          ) {
            checkOnce(genCustomer) { customer =>
              def sink: Sink[Throwable, Item, Nothing, Chunk[Item]] =
                ZSink.collectAll[Item]

              for {
                _ <- ElasticExecutor.execute(ElasticRequest.deleteByQuery(secondSearchIndex, matchAll))
                reqs = (0 to 200).map { _ =>
                         ElasticRequest.create[CustomerDocument](
                           secondSearchIndex,
                           customer.copy(id = Random.alphanumeric.take(5).mkString, balance = 150)
                         )
                       }
                _    <- ElasticExecutor.execute(ElasticRequest.bulk(reqs: _*).refreshTrue)
                query = range("balance").gte(100)
                res <- ElasticExecutor
                         .stream(
                           ElasticRequest.search(secondSearchIndex, query),
                           StreamConfig.searchAfter.withPageSize(40).keepAliveFor("2m")
                         )
                         .run(sink)
              } yield assert(res)(hasSize(equalTo(201)))
            }
          } @@ around(
            ElasticExecutor.execute(ElasticRequest.createIndex(secondSearchIndex)),
            ElasticExecutor.execute(ElasticRequest.deleteIndex(secondSearchIndex)).orDie
          ),
          test("successfully create PIT(point in time) and return stream results as specific type") {
            checkOnce(genCustomer) { customer =>
              def sink: Sink[Throwable, CustomerDocument, Nothing, Chunk[CustomerDocument]] =
                ZSink.collectAll[CustomerDocument]

              for {
                _ <- ElasticExecutor.execute(ElasticRequest.deleteByQuery(secondSearchIndex, matchAll))
                reqs = (0 to 200).map { _ =>
                         ElasticRequest.create[CustomerDocument](
                           secondSearchIndex,
                           customer.copy(id = Random.alphanumeric.take(5).mkString, balance = 150)
                         )
                       }
                _    <- ElasticExecutor.execute(ElasticRequest.bulk(reqs: _*).refreshTrue)
                query = range("balance").gte(100)
                res <- ElasticExecutor
                         .streamAs[CustomerDocument](
                           ElasticRequest.search(secondSearchIndex, query),
                           StreamConfig.searchAfter
                         )
                         .run(sink)
              } yield assert(res)(hasSize(equalTo(201)))
            }
          } @@ around(
            ElasticExecutor.execute(ElasticRequest.createIndex(secondSearchIndex)),
            ElasticExecutor.execute(ElasticRequest.deleteIndex(secondSearchIndex)).orDie
          ),
          test("successfully create point in time and return empty stream if there is no valid results") {
            checkOnce(genCustomer) { customer =>
              def sink: Sink[Throwable, Item, Nothing, Chunk[Item]] =
                ZSink.collectAll[Item]

              for {
                _ <- ElasticExecutor.execute(ElasticRequest.deleteByQuery(secondSearchIndex, matchAll))
                _ <- ElasticExecutor.execute(
                       ElasticRequest
                         .create[CustomerDocument](
                           secondSearchIndex,
                           customer.copy(id = Random.alphanumeric.take(5).mkString, balance = 150)
                         )
                         .refreshTrue
                     )
                query = range("balance").gte(200)
                res <- ElasticExecutor
                         .stream(ElasticRequest.search(secondSearchIndex, query), StreamConfig.searchAfter)
                         .run(sink)
              } yield assert(res)(isEmpty)
            }
          } @@ around(
            ElasticExecutor.execute(ElasticRequest.createIndex(secondSearchIndex)),
            ElasticExecutor.execute(ElasticRequest.deleteIndex(secondSearchIndex)).orDie
          )
        ) @@ shrinks(0),
        suite("deleting by query")(
          test("successfully delete all matched documents") {
            checkOnce(genDocumentId, genCustomer, genDocumentId, genCustomer, genDocumentId, genCustomer) {
              (firstDocumentId, firstCustomer, secondDocumentId, secondCustomer, thirdDocumentId, thirdCustomer) =>
                for {
                  _ <- ElasticExecutor.execute(
                         ElasticRequest
                           .upsert[CustomerDocument](
                             deleteByQueryIndex,
                             firstDocumentId,
                             firstCustomer.copy(balance = 150)
                           )
                       )
                  _ <- ElasticExecutor.execute(
                         ElasticRequest
                           .upsert[CustomerDocument](
                             deleteByQueryIndex,
                             secondDocumentId,
                             secondCustomer.copy(balance = 350)
                           )
                       )
                  _ <- ElasticExecutor.execute(
                         ElasticRequest
                           .upsert[CustomerDocument](
                             deleteByQueryIndex,
                             thirdDocumentId,
                             thirdCustomer.copy(balance = 400)
                           )
                           .refreshTrue
                       )
                  deleteQuery = range("balance").gte(300)
                  _ <- ElasticExecutor
                         .execute(ElasticRequest.deleteByQuery(deleteByQueryIndex, deleteQuery).refreshTrue)
                  res <- ElasticExecutor
                           .execute(ElasticRequest.search(deleteByQueryIndex, matchAll))
                           .documentAs[CustomerDocument]
                } yield assert(res)(hasSameElements(List(firstCustomer.copy(balance = 150))))
            }
          } @@ around(
            ElasticExecutor.execute(ElasticRequest.createIndex(deleteByQueryIndex)),
            ElasticExecutor.execute(ElasticRequest.deleteIndex(deleteByQueryIndex)).orDie
          ),
          test("returns NotFound when provided index is missing") {
            checkOnce(genIndexName) { missingIndex =>
              assertZIO(ElasticExecutor.execute(ElasticRequest.deleteByQuery(missingIndex, matchAll)))(
                equalTo(NotFound)
              )
            }
          }
        ),
        suite("bulk query")(
          test("successfully execute bulk query") {
            checkOnce(genDocumentId, genDocumentId, genDocumentId, genCustomer) {
              (firstDocId, secondDocId, thirdDocId, customer) =>
                for {
                  _ <- ElasticExecutor.execute(
                         ElasticRequest
                           .create[CustomerDocument](index, firstDocId, customer.copy(id = "randomIdString"))
                       )
                  _ <- ElasticExecutor.execute(
                         ElasticRequest
                           .create[CustomerDocument](index, secondDocId, customer.copy(id = "randomIdString2"))
                           .refreshTrue
                       )
                  req1 = ElasticRequest.create[CustomerDocument](index, thirdDocId, customer)
                  req2 = ElasticRequest.create[CustomerDocument](index, customer.copy(id = "randomIdString3"))
                  req3 = ElasticRequest.upsert[CustomerDocument](index, firstDocId, customer.copy(balance = 3000))
                  req4 = ElasticRequest.deleteById(index, secondDocId)
                  res <- ElasticExecutor.execute(ElasticRequest.bulk(req1, req2, req3, req4))
                } yield assert(res)(isUnit)
            }
          }
        )
      ) @@ nondeterministic @@ sequential @@ prepareElasticsearchIndexForTests @@ afterAll(
        ElasticExecutor.execute(ElasticRequest.deleteIndex(index)).orDie
      )
    ).provideShared(
      elasticsearchLayer
    )
  }
}
