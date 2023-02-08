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

import zio.Scope
import zio.elasticsearch.ElasticQuery._
import zio.elasticsearch.ElasticRequest.BulkRequest
import zio.elasticsearch.utils._
import zio.prelude.Newtype.unsafeWrap
import zio.prelude.Validation
import zio.schema.{DeriveSchema, Schema}
import zio.test.Assertion.equalTo
import zio.test._

object QueryDSLSpec extends ZIOSpecDefault {

  final case class UserDocument(id: String, name: String, address: String, balance: Double, age: Int)

  object UserDocument {

    implicit val schema: Schema.CaseClass5[String, String, String, Double, Int, UserDocument] =
      DeriveSchema.gen[UserDocument]

    val (id, name, address, balance, age) = schema.makeAccessors(ElasticQueryAccessorBuilder)
  }

  override def spec: Spec[Environment with TestEnvironment with Scope, Any] =
    suite("Query DSL")(
      suite("creating ElasticQuery")(
        test("successfully create Match query using `matches` method") {
          val queryString = matches(field = "day_of_week", value = "Monday")
          val queryBool   = matches(field = "day_of_week", value = true)
          val queryLong   = matches(field = "day_of_week", value = 1L)

          assert(queryString)(equalTo(MatchQuery(field = "day_of_week", value = "Monday"))) &&
          assert(queryBool)(equalTo(MatchQuery(field = "day_of_week", value = true))) &&
          assert(queryLong)(equalTo(MatchQuery(field = "day_of_week", value = 1)))
        },
        test("successfully create type-safe Match query using `matches` method") {
          val queryString = matches(field = UserDocument.name, value = "Name")
          val queryInt    = matches(field = UserDocument.age, value = 39)

          assert(queryString)(equalTo(MatchQuery(field = "name", value = "Name"))) &&
          assert(queryInt)(equalTo(MatchQuery(field = "age", value = 39)))
        },
        test("successfully create type-safe Match query with multi-field using `matches` method") {
          val query = matches(field = UserDocument.name, multiField = Some("keyword"), value = "Name")

          assert(query)(equalTo(MatchQuery(field = "name.keyword", value = "Name")))
        },
        test("successfully create Bool Query with boost") {
          val query = boolQuery().boost(1.0)

          assert(query)(equalTo(BoolQuery(filter = Nil, must = Nil, should = Nil, boost = Some(1.0))))
        },
        test("successfully create `Filter` query from two Match queries") {
          val query = boolQuery
            .filter(
              matches(field = "day_of_week", value = "Monday"),
              matches(field = "customer_gender", value = "MALE")
            )

          assert(query)(
            equalTo(
              BoolQuery(
                filter = List(
                  MatchQuery(field = "day_of_week", value = "Monday"),
                  MatchQuery(field = "customer_gender", value = "MALE")
                ),
                must = Nil,
                should = Nil
              )
            )
          )
        },
        test("successfully create `Must` query from two Match queries") {
          val query = boolQuery
            .must(matches(field = "day_of_week", value = "Monday"), matches(field = "customer_gender", value = "MALE"))

          assert(query)(
            equalTo(
              BoolQuery(
                filter = Nil,
                must = List(
                  MatchQuery(field = "day_of_week", value = "Monday"),
                  MatchQuery(field = "customer_gender", value = "MALE")
                ),
                should = Nil
              )
            )
          )
        },
        test("successfully create `Should` query from two Match queries") {
          val query = boolQuery
            .should(
              matches(field = "day_of_week", value = "Monday"),
              matches(field = "customer_gender", value = "MALE")
            )

          assert(query)(
            equalTo(
              BoolQuery(
                filter = Nil,
                must = Nil,
                should = List(
                  MatchQuery(field = "day_of_week", value = "Monday"),
                  MatchQuery(field = "customer_gender", value = "MALE")
                )
              )
            )
          )
        },
        test("successfully create `Filter/Must/Should` mixed query with Filter containing two Match queries") {
          val query = boolQuery
            .filter(
              matches(field = "day_of_week", value = "Monday"),
              matches(field = "customer_gender", value = "MALE")
            )
            .must(matches(field = "customer_age", value = 23))
            .should(matches(field = "customer_id", value = 1))

          assert(query)(
            equalTo(
              BoolQuery(
                filter = List(
                  MatchQuery(field = "day_of_week", value = "Monday"),
                  MatchQuery(field = "customer_gender", value = "MALE")
                ),
                must = List(MatchQuery(field = "customer_age", value = 23)),
                should = List(MatchQuery(field = "customer_id", value = 1))
              )
            )
          )
        },
        test("successfully create `Filter/Must/Should` mixed query with Must containing two Match queries") {
          val query = boolQuery
            .filter(matches(field = "customer_id", value = 1))
            .must(matches(field = "day_of_week", value = "Monday"), matches(field = "customer_gender", value = "MALE"))
            .should(matches(field = "customer_age", value = 23))

          assert(query)(
            equalTo(
              BoolQuery(
                filter = List(MatchQuery(field = "customer_id", value = 1)),
                must = List(
                  MatchQuery(field = "day_of_week", value = "Monday"),
                  MatchQuery(field = "customer_gender", value = "MALE")
                ),
                should = List(MatchQuery(field = "customer_age", value = 23))
              )
            )
          )
        },
        test("successfully create `Filter/Must/Should` mixed query with Should containing two Match queries") {
          val query = boolQuery
            .filter(matches(field = "customer_id", value = 1))
            .must(matches(field = "customer_age", value = 23))
            .should(
              matches(field = "day_of_week", value = "Monday"),
              matches(field = "customer_gender", value = "MALE")
            )

          assert(query)(
            equalTo(
              BoolQuery(
                filter = List(MatchQuery(field = "customer_id", value = 1)),
                must = List(MatchQuery(field = "customer_age", value = 23)),
                should = List(
                  MatchQuery(field = "day_of_week", value = "Monday"),
                  MatchQuery(field = "customer_gender", value = "MALE")
                )
              )
            )
          )
        },
        test("successfully create `Filter/Must/Should` mixed query with boost") {
          val query = boolQuery()
            .filter(matches(field = "customer_id", value = 1))
            .must(matches(field = "customer_age", value = 23))
            .should(matches(field = "day_of_week", value = "Monday"))
            .boost(1.0)

          assert(query)(
            equalTo(
              BoolQuery(
                filter = List(MatchQuery(field = "customer_id", value = 1)),
                must = List(MatchQuery(field = "customer_age", value = 23)),
                should = List(MatchQuery(field = "day_of_week", value = "Monday")),
                boost = Some(1.0)
              )
            )
          )
        },
        test("successfully create Exists Query") {
          val query = exists(field = "day_of_week")

          assert(query)(equalTo(ExistsQuery(field = "day_of_week")))
        },
        test("successfully create Exists Query with accessor") {
          val query = exists(field = UserDocument.name)

          assert(query)(equalTo(ExistsQuery(field = "name")))
        },
        test("successfully create MatchAll Query") {
          val query = matchAll

          assert(query)(equalTo(MatchAllQuery(None)))
        },
        test("successfully create MatchAll Query with boost") {
          val query = matchAll.boost(1.0)

          assert(query)(equalTo(MatchAllQuery(boost = Some(1.0))))
        },
        test("successfully create empty Range Query") {
          val query = range(field = "customer_age")

          assert(query)(
            equalTo(
              RangeQuery[Any, Unbounded.type, Unbounded.type](
                field = "customer_age",
                lower = Unbounded,
                upper = Unbounded
              )
            )
          )
        },
        test("successfully create empty type-safe Range Query") {
          val queryString = range(field = UserDocument.name)
          val queryInt    = range(field = UserDocument.age)

          assert(queryString)(
            equalTo(
              RangeQuery[String, Unbounded.type, Unbounded.type](field = "name", lower = Unbounded, upper = Unbounded)
            )
          ) &&
          assert(queryInt)(
            equalTo(
              RangeQuery[Int, Unbounded.type, Unbounded.type](field = "age", lower = Unbounded, upper = Unbounded)
            )
          )
        },
        test("successfully create empty type-safe Range Query with multi-field") {
          val query = range(field = UserDocument.name, multiField = Some("keyword"))

          assert(query)(
            equalTo(
              RangeQuery[String, Unbounded.type, Unbounded.type](
                field = "name.keyword",
                lower = Unbounded,
                upper = Unbounded
              )
            )
          )
        },
        test("successfully create Range Query with upper bound") {
          val query = range(field = "customer_age").lt(23)

          assert(query)(
            equalTo(
              RangeQuery[Int, Unbounded.type, LessThan[Int]](
                field = "customer_age",
                lower = Unbounded,
                upper = LessThan(23)
              )
            )
          )
        },
        test("successfully create Range Query with lower bound") {
          val query = range(field = "customer_age").gt(23)

          assert(query)(
            equalTo(
              RangeQuery[Int, GreaterThan[Int], Unbounded.type](
                field = "customer_age",
                lower = GreaterThan(23),
                upper = Unbounded
              )
            )
          )
        },
        test("successfully create Range Query with inclusive upper bound") {
          val query = range(field = "customer_age").lte(23)

          assert(query)(
            equalTo(
              RangeQuery[Int, Unbounded.type, LessThanOrEqualTo[Int]](
                field = "customer_age",
                lower = Unbounded,
                upper = LessThanOrEqualTo(23)
              )
            )
          )
        },
        test("successfully create Range Query with inclusive lower bound") {
          val query = range(field = "customer_age").gte(23)

          assert(query)(
            equalTo(
              RangeQuery[Int, GreaterThanOrEqualTo[Int], Unbounded.type](
                field = "customer_age",
                lower = GreaterThanOrEqualTo(23),
                upper = Unbounded
              )
            )
          )
        },
        test("successfully create Range Query with both upper and lower bound") {
          val query = range(field = "customer_age").gte(23).lt(50)

          assert(query)(
            equalTo(
              RangeQuery[Int, GreaterThanOrEqualTo[Int], LessThan[Int]](
                field = "customer_age",
                lower = GreaterThanOrEqualTo(23),
                upper = LessThan(50)
              )
            )
          )
        },
        test("successfully create Term Query") {
          val queryInt    = term(field = "day_of_week", value = 1)
          val queryString = term(field = "day_of_week", value = "Monday")
          val queryBool   = term(field = "day_of_week", value = true)
          val queryLong   = term(field = "day_of_week", value = 1L)

          assert(queryInt)(
            equalTo(TermQuery(field = "day_of_week", value = 1, boost = None, caseInsensitive = None))
          ) &&
          assert(queryString)(
            equalTo(TermQuery(field = "day_of_week", value = "Monday", boost = None, caseInsensitive = None))
          ) &&
          assert(queryBool)(
            equalTo(TermQuery(field = "day_of_week", value = true, boost = None, caseInsensitive = None))
          ) &&
          assert(queryLong)(equalTo(TermQuery(field = "day_of_week", value = 1L, boost = None, caseInsensitive = None)))
        },
        test("successfully create type-safe Term Query") {
          val queryString = term(field = UserDocument.name, value = "Name")
          val queryInt    = term(field = UserDocument.age, value = 39)

          assert(queryString)(
            equalTo(TermQuery(field = "name", value = "Name", boost = None, caseInsensitive = None))
          ) &&
          assert(queryInt)(equalTo(TermQuery(field = "age", value = 39, boost = None, caseInsensitive = None)))
        },
        test("successfully create type-safe Term Query with multi-field") {
          val query = term(field = UserDocument.name, multiField = Some("keyword"), value = "Name")

          assert(query)(
            equalTo(TermQuery(field = "name.keyword", value = "Name", boost = None, caseInsensitive = None))
          )
        },
        test("successfully create Term Query with boost") {
          val queryInt    = term(field = "day_of_week", value = 1).boost(1.0)
          val queryString = term(field = "day_of_week", value = "Monday").boost(1.0)
          val queryBool   = term(field = "day_of_week", value = true).boost(1.0)
          val queryLong   = term(field = "day_of_week", value = 1L).boost(1.0)

          assert(queryInt)(
            equalTo(TermQuery(field = "day_of_week", value = 1, boost = Some(1.0), caseInsensitive = None))
          ) &&
          assert(queryString)(
            equalTo(TermQuery(field = "day_of_week", value = "Monday", boost = Some(1.0), caseInsensitive = None))
          ) &&
          assert(queryBool)(
            equalTo(TermQuery(field = "day_of_week", value = true, boost = Some(1.0), caseInsensitive = None))
          ) &&
          assert(queryLong)(
            equalTo(TermQuery(field = "day_of_week", value = 1L, boost = Some(1.0), caseInsensitive = None))
          )
        },
        test("successfully create case insensitive Term Query") {
          val queryString = term(field = "day_of_week", value = "Monday").caseInsensitiveTrue

          assert(queryString)(
            equalTo(TermQuery(field = "day_of_week", value = "Monday", boost = None, caseInsensitive = Some(true)))
          )
        },
        test("successfully create case insensitive Term Query with boost") {
          val queryString = term(field = "day_of_week", value = "Monday").boost(1.0).caseInsensitiveTrue

          assert(queryString)(
            equalTo(TermQuery(field = "day_of_week", value = "Monday", boost = Some(1.0), caseInsensitive = Some(true)))
          )
        },
        test("successfully create Wildcard Query") {
          val wildcardQuery1 = contains(field = "day_of_week", value = "M")
          val wildcardQuery2 = startsWith(field = "day_of_week", value = "M")
          val wildcardQuery3 = wildcard(field = "day_of_week", value = "M*")

          assert(wildcardQuery1)(
            equalTo(WildcardQuery(field = "day_of_week", value = "*M*", boost = None, caseInsensitive = None))
          ) &&
          assert(wildcardQuery2)(
            equalTo(WildcardQuery(field = "day_of_week", value = "M*", boost = None, caseInsensitive = None))
          ) &&
          assert(wildcardQuery3)(
            equalTo(WildcardQuery(field = "day_of_week", value = "M*", boost = None, caseInsensitive = None))
          )
        },
        test("successfully create Wildcard Query with boost") {
          val wildcardQuery1 = contains(field = "day_of_week", value = "M").boost(1.0)
          val wildcardQuery2 = startsWith(field = "day_of_week", value = "M").boost(1.0)
          val wildcardQuery3 = wildcard(field = "day_of_week", value = "M*").boost(1.0)

          assert(wildcardQuery1)(
            equalTo(WildcardQuery(field = "day_of_week", value = "*M*", boost = Some(1.0), caseInsensitive = None))
          ) &&
          assert(wildcardQuery2)(
            equalTo(WildcardQuery(field = "day_of_week", value = "M*", boost = Some(1.0), caseInsensitive = None))
          ) &&
          assert(wildcardQuery3)(
            equalTo(WildcardQuery(field = "day_of_week", value = "M*", boost = Some(1.0), caseInsensitive = None))
          )
        },
        test("successfully create case insensitive Wildcard Query") {
          val wildcardQuery1 = contains(field = "day_of_week", value = "M").caseInsensitiveTrue
          val wildcardQuery2 = startsWith(field = "day_of_week", value = "M").caseInsensitiveTrue
          val wildcardQuery3 = wildcard(field = "day_of_week", value = "M*").caseInsensitiveTrue

          assert(wildcardQuery1)(
            equalTo(WildcardQuery(field = "day_of_week", value = "*M*", boost = None, caseInsensitive = Some(true)))
          ) &&
          assert(wildcardQuery2)(
            equalTo(WildcardQuery(field = "day_of_week", value = "M*", boost = None, caseInsensitive = Some(true)))
          ) &&
          assert(wildcardQuery3)(
            equalTo(WildcardQuery(field = "day_of_week", value = "M*", boost = None, caseInsensitive = Some(true)))
          )
        },
        test("successfully create case insensitive Wildcard Query with boost") {
          val wildcardQuery1 = contains(field = "day_of_week", value = "M").boost(1.0).caseInsensitiveTrue
          val wildcardQuery2 = startsWith(field = "day_of_week", value = "M").boost(1.0).caseInsensitiveTrue
          val wildcardQuery3 = wildcard(field = "day_of_week", value = "M*").boost(1.0).caseInsensitiveTrue

          assert(wildcardQuery1)(
            equalTo(
              WildcardQuery(field = "day_of_week", value = "*M*", boost = Some(1.0), caseInsensitive = Some(true))
            )
          ) &&
          assert(wildcardQuery2)(
            equalTo(WildcardQuery(field = "day_of_week", value = "M*", boost = Some(1.0), caseInsensitive = Some(true)))
          ) &&
          assert(wildcardQuery3)(
            equalTo(WildcardQuery(field = "day_of_week", value = "M*", boost = Some(1.0), caseInsensitive = Some(true)))
          )
        }
      ),
      suite("encoding ElasticQuery as JSON")(
        test("properly encode Match query") {
          val query = matches(field = "day_of_week", value = true)
          val expected =
            """
              |{
              |  "query": {
              |    "match": {
              |      "day_of_week": true
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJson)(equalTo(expected.toJson))
        },
        test("properly encode Bool Query with boost") {
          val query = boolQuery().boost(1.0)
          val expected =
            """
              |{
              |  "query": {
              |    "bool": {
              |      "filter": [],
              |      "must": [],
              |      "should": [],
              |      "boost": 1.0
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJsonBody)(equalTo(expected.toJson))
        },
        test("properly encode Bool Query with Filter containing `Match` leaf query") {
          val query = boolQuery.filter(matches(field = "day_of_week", value = "Monday"))
          val expected =
            """
              |{
              |  "query": {
              |    "bool": {
              |      "filter": [
              |        {
              |          "match": {
              |            "day_of_week": "Monday"
              |          }
              |        }
              |      ],
              |      "must": [],
              |      "should": []
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJson)(equalTo(expected.toJson))
        },
        test("properly encode Bool Query with Must containing `Match` leaf query") {
          val query = boolQuery.must(matches(field = "day_of_week", value = "Monday"))
          val expected =
            """
              |{
              |  "query": {
              |    "bool": {
              |      "filter": [],
              |      "must": [
              |        {
              |          "match": {
              |            "day_of_week": "Monday"
              |          }
              |        }
              |      ],
              |      "should": []
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJson)(equalTo(expected.toJson))
        },
        test("properly encode Bool Query with Should containing `Match` leaf query") {
          val query = boolQuery.should(matches(field = "day_of_week", value = "Monday"))
          val expected =
            """
              |{
              |  "query": {
              |    "bool": {
              |      "filter": [],
              |      "must": [],
              |      "should": [
              |        {
              |          "match": {
              |            "day_of_week": "Monday"
              |          }
              |        }
              |      ]
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJson)(equalTo(expected.toJson))
        },
        test("properly encode Bool Query with Filter, Must and Should containing `Match` leaf query") {
          val query = boolQuery
            .filter(matches(field = "customer_age", value = 23))
            .must(matches(field = "customer_id", value = 1))
            .should(matches(field = "day_of_week", value = "Monday"))
          val expected =
            """
              |{
              |  "query": {
              |    "bool": {
              |      "filter": [
              |        {
              |          "match": {
              |            "customer_age": 23
              |          }
              |        }
              |      ],
              |      "must": [
              |        {
              |          "match": {
              |            "customer_id": 1
              |          }
              |        }
              |      ],
              |      "should": [
              |        {
              |          "match": {
              |            "day_of_week": "Monday"
              |          }
              |        }
              |      ]
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJson)(equalTo(expected.toJson))
        },
        test("properly encode Bool Query with Filter, Must and Should containing `Match` leaf query and with boost") {
          val query = boolQuery()
            .filter(matches(field = "customer_age", value = 23))
            .must(matches(field = "customer_id", value = 1))
            .should(matches(field = "day_of_week", value = "Monday"))
            .boost(1.0)
          val expected =
            """
              |{
              |  "query": {
              |    "bool": {
              |      "filter": [
              |        {
              |          "match": {
              |            "customer_age": 23
              |          }
              |        }
              |      ],
              |      "must": [
              |        {
              |          "match": {
              |            "customer_id": 1
              |          }
              |        }
              |      ],
              |      "should": [
              |        {
              |          "match": {
              |            "day_of_week": "Monday"
              |          }
              |        }
              |      ],
              |      "boost": 1.0
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJsonBody)(equalTo(expected.toJson))
        },
        test("properly encode Exists Query") {
          val query = exists(field = "day_of_week")
          val expected =
            """
              |{
              |  "query": {
              |    "exists": {
              |      "field": "day_of_week"
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJson)(equalTo(expected.toJson))
        },
        test("properly encode MatchAll Query") {
          val query = matchAll
          val expected =
            """
              |{
              |  "query": {
              |    "match_all": {}
              |  }
              |}
              |""".stripMargin

          assert(query.toJson)(equalTo(expected.toJson))
        },
        test("properly encode MatchAll Query with boost") {
          val query = matchAll.boost(1.0)
          val expected =
            """
              |{
              |  "query": {
              |    "match_all": {
              |      "boost": 1.0
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJson)(equalTo(expected.toJson))
        },
        test("properly encode Unbounded Range Query") {
          val query = range(field = "field")
          val expected =
            """
              |{
              |  "query": {
              |    "range": {
              |      "field": {
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJson)(equalTo(expected.toJson))
        },
        test("properly encode Unbounded Range Query with boost") {
          val query = range(field = "field").boost(1.0)
          val expected =
            """
              |{
              |  "query": {
              |    "range": {
              |      "field": {
              |      },
              |      "boost": 1.0
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJsonBody)(equalTo(expected.toJson))
        },
        test("properly encode Range Query with Lower Bound") {
          val query = range(field = "customer_age").gt(23)
          val expected =
            """
              |{
              |  "query": {
              |    "range": {
              |      "customer_age": {
              |        "gt": 23
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJson)(equalTo(expected.toJson))
        },
        test("properly encode Range Query with Upper Bound") {
          val query = range(field = "customer_age").lt(23)
          val expected =
            """
              |{
              |  "query": {
              |    "range": {
              |      "customer_age": {
              |        "lt": 23
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJson)(equalTo(expected.toJson))
        },
        test("properly encode Range Query with Inclusive Lower Bound") {
          val query = range(field = "expiry_date").gte("now")
          val expected =
            """
              |{
              |  "query": {
              |    "range": {
              |      "expiry_date": {
              |        "gte": "now"
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJson)(equalTo(expected.toJson))
        },
        test("properly encode Range Query with inclusive Upper Bound") {
          val query = range(field = "customer_age").lte(100L)
          val expected =
            """
              |{
              |  "query": {
              |    "range": {
              |      "customer_age": {
              |        "lte": 100
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJson)(equalTo(expected.toJson))
        },
        test("properly encode Range Query with both Upper and Lower Bound") {
          val query = range(field = "customer_age").gte(10).lt(100)
          val expected =
            """
              |{
              |  "query": {
              |    "range": {
              |      "customer_age": {
              |        "gte": 10,
              |        "lt": 100
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJson)(equalTo(expected.toJson))
        },
        test("properly encode Range Query with both Upper and Lower Bound with boost") {
          val query = range(field = "customer_age").gte(10).lt(100).boost(1.0)
          val expected =
            """
              |{
              |  "query": {
              |    "range": {
              |      "customer_age": {
              |        "gte": 10,
              |        "lt": 100
              |      },
              |      "boost": 1.0
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJsonBody)(equalTo(expected.toJson))
        },
        test("properly encode Term query") {
          val query = term(field = "day_of_week", value = true)
          val expected =
            """
              |{
              |  "query": {
              |    "term": {
              |      "day_of_week": {
              |        "value": true
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJson)(equalTo(expected.toJson))
        },
        test("properly encode Term query with boost") {
          val query = term(field = "day_of_week", value = true).boost(1.0)
          val expected =
            """
              |{
              |  "query": {
              |    "term": {
              |      "day_of_week": {
              |        "value": true,
              |        "boost": 1.0
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJson)(equalTo(expected.toJson))
        },
        test("properly encode case insensitive Term query") {
          val query = term(field = "day_of_week", value = "Monday").caseInsensitiveTrue
          val expected =
            """
              |{
              |  "query": {
              |    "term": {
              |      "day_of_week": {
              |        "value": "Monday",
              |        "case_insensitive": true
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJson)(equalTo(expected.toJson))
        },
        test("properly encode case insensitive Term query with boost") {
          val query = term(field = "day_of_week", value = "Monday").boost(1.0).caseInsensitiveTrue
          val expected =
            """
              |{
              |  "query": {
              |    "term": {
              |      "day_of_week": {
              |        "value": "Monday",
              |        "boost": 1.0,
              |        "case_insensitive": true
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJson)(equalTo(expected.toJson))
        },
        test("properly encode Wildcard query") {
          val query1 = contains(field = "day_of_week", value = "M")
          val query2 = startsWith(field = "day_of_week", value = "M")
          val query3 = wildcard(field = "day_of_week", value = "M*")
          val expected1 =
            """
              |{
              |  "query": {
              |    "wildcard": {
              |      "day_of_week": {
              |        "value": "*M*"
              |      }
              |    }
              |  }
              |}
              |""".stripMargin
          val expected23 =
            """
              |{
              |  "query": {
              |    "wildcard": {
              |      "day_of_week": {
              |        "value": "M*"
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query1.toJson)(equalTo(expected1.toJson)) &&
          assert(query2.toJson)(equalTo(expected23.toJson)) &&
          assert(query3.toJson)(equalTo(expected23.toJson))
        },
        test("properly encode Wildcard query with boost") {
          val query1 = contains(field = "day_of_week", value = "M").boost(1.0)
          val query2 = startsWith(field = "day_of_week", value = "M").boost(1.0)
          val query3 = wildcard(field = "day_of_week", value = "M*").boost(1.0)
          val expected1 =
            """
              |{
              |  "query": {
              |    "wildcard": {
              |      "day_of_week": {
              |        "value": "*M*",
              |        "boost": 1.0
              |      }
              |    }
              |  }
              |}
              |""".stripMargin
          val expected23 =
            """
              |{
              |  "query": {
              |    "wildcard": {
              |      "day_of_week": {
              |        "value": "M*",
              |        "boost": 1.0
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query1.toJson)(equalTo(expected1.toJson)) &&
          assert(query2.toJson)(equalTo(expected23.toJson)) &&
          assert(query3.toJson)(equalTo(expected23.toJson))
        },
        test("properly encode case insensitive Wildcard query") {
          val query1 = contains(field = "day_of_week", value = "M").caseInsensitiveTrue
          val query2 = startsWith(field = "day_of_week", value = "M").caseInsensitiveTrue
          val query3 = wildcard(field = "day_of_week", value = "M*").caseInsensitiveTrue
          val expected1 =
            """
              |{
              |  "query": {
              |    "wildcard": {
              |      "day_of_week": {
              |        "value": "*M*",
              |        "case_insensitive": true
              |      }
              |    }
              |  }
              |}
              |""".stripMargin
          val expected23 =
            """
              |{
              |  "query": {
              |    "wildcard": {
              |      "day_of_week": {
              |        "value": "M*",
              |        "case_insensitive": true
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query1.toJson)(equalTo(expected1.toJson)) &&
          assert(query2.toJson)(equalTo(expected23.toJson)) &&
          assert(query3.toJson)(equalTo(expected23.toJson))
        },
        test("properly encode case insensitive Wildcard query with boost") {
          val query1 = contains(field = "day_of_week", value = "M").boost(1.0).caseInsensitiveTrue
          val query2 = startsWith(field = "day_of_week", value = "M").boost(1.0).caseInsensitiveTrue
          val query3 = wildcard(field = "day_of_week", value = "M*").boost(1.0).caseInsensitiveTrue
          val expected1 =
            """
              |{
              |  "query": {
              |    "wildcard": {
              |      "day_of_week": {
              |        "value": "*M*",
              |        "boost": 1.0,
              |        "case_insensitive": true
              |      }
              |    }
              |  }
              |}
              |""".stripMargin
          val expected23 =
            """
              |{
              |  "query": {
              |    "wildcard": {
              |      "day_of_week": {
              |        "value": "M*",
              |        "boost": 1.0,
              |        "case_insensitive": true
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query1.toJson)(equalTo(expected1.toJson)) &&
          assert(query2.toJson)(equalTo(expected23.toJson)) &&
          assert(query3.toJson)(equalTo(expected23.toJson))
        },
        test("properly encode Bulk request body") {
          val bulkQuery = IndexName.make("users").map { index =>
            val user =
              UserDocument(id = "WeeMwR5d5", name = "Name", address = "Address", balance = 1000, age = 24)
            val req1 =
              ElasticRequest
                .create[UserDocument](index, DocumentId("ETux1srpww2ObCx"), user.copy(age = 39))
                .routing(unsafeWrap(Routing)(user.id))
            val req2 = ElasticRequest.create[UserDocument](index, user).routing(unsafeWrap(Routing)(user.id))
            val req3 =
              ElasticRequest
                .upsert[UserDocument](index, DocumentId("yMyEG8iFL5qx"), user.copy(balance = 3000))
                .routing(unsafeWrap(Routing)(user.id))
            val req4 =
              ElasticRequest.deleteById(index, DocumentId("1VNzFt2XUFZfXZheDc")).routing(unsafeWrap(Routing)(user.id))
            ElasticRequest.bulk(req1, req2, req3, req4) match {
              case r: BulkRequest => Some(r.body)
              case _              => None
            }
          }

          val expectedBody =
            """|{ "create" : { "_index" : "users", "_id" : "ETux1srpww2ObCx", "routing" : "WeeMwR5d5" } }
               |{"id":"WeeMwR5d5","name":"Name","address":"Address","balance":1000.0,"age":39}
               |{ "create" : { "_index" : "users", "routing" : "WeeMwR5d5" } }
               |{"id":"WeeMwR5d5","name":"Name","address":"Address","balance":1000.0,"age":24}
               |{ "index" : { "_index" : "users", "_id" : "yMyEG8iFL5qx", "routing" : "WeeMwR5d5" } }
               |{"id":"WeeMwR5d5","name":"Name","address":"Address","balance":3000.0,"age":24}
               |{ "delete" : { "_index" : "users", "_id" : "1VNzFt2XUFZfXZheDc", "routing" : "WeeMwR5d5" } }
               |""".stripMargin

          assert(bulkQuery)(equalTo(Validation.succeed(Some(expectedBody))))
        }
      )
    )
}
