package zio.elasticsearch

import zio.Scope
import zio.elasticsearch.ElasticQuery._
import zio.elasticsearch.ElasticRequest.BulkRequest
import zio.elasticsearch.utils._
import zio.test.Assertion.{equalTo, isSome}
import zio.test._

object QueryDSLSpec extends ZIOSpecDefault {
  override def spec: Spec[Environment with TestEnvironment with Scope, Any] =
    suite("Query DSL")(
      suite("creating ElasticQuery")(
        test("successfully create Match query using `matches` method") {
          val queryString = matches(field = "day_of_week", value = "Monday")
          val queryBool   = matches(field = "day_of_week", value = true)
          val queryLong   = matches(field = "day_of_week", value = 1L)

          assert(queryString)(equalTo(MatchQuery(field = "day_of_week", value = "Monday")))
          assert(queryBool)(equalTo(MatchQuery(field = "day_of_week", value = true)))
          assert(queryLong)(equalTo(MatchQuery(field = "day_of_week", value = 1)))
        },
        test("successfully create `Must` query from two Match queries") {
          val query = boolQuery()
            .must(matches(field = "day_of_week", value = "Monday"), matches(field = "customer_gender", value = "MALE"))

          assert(query)(
            equalTo(
              BoolQuery(
                must = List(
                  MatchQuery(field = "day_of_week", value = "Monday"),
                  MatchQuery(field = "customer_gender", value = "MALE")
                ),
                should = List.empty
              )
            )
          )
        },
        test("successfully create `Should` query from two Match queries") {
          val query = boolQuery()
            .should(
              matches(field = "day_of_week", value = "Monday"),
              matches(field = "customer_gender", value = "MALE")
            )

          assert(query)(
            equalTo(
              BoolQuery(
                must = List.empty,
                should = List(
                  MatchQuery(field = "day_of_week", value = "Monday"),
                  MatchQuery(field = "customer_gender", value = "MALE")
                )
              )
            )
          )
        },
        test("successfully create `Must/Should` mixed query") {
          val query = boolQuery()
            .must(matches(field = "day_of_week", value = "Monday"), matches(field = "customer_gender", value = "MALE"))
            .should(matches(field = "customer_age", value = 23))

          assert(query)(
            equalTo(
              BoolQuery(
                must = List(
                  MatchQuery(field = "day_of_week", value = "Monday"),
                  MatchQuery(field = "customer_gender", value = "MALE")
                ),
                should = List(MatchQuery(field = "customer_age", value = 23))
              )
            )
          )
        },
        test("successfully create `Should/Must` mixed query") {
          val query = boolQuery()
            .must(matches(field = "customer_age", value = 23))
            .should(
              matches(field = "day_of_week", value = "Monday"),
              matches(field = "customer_gender", value = "MALE")
            )

          assert(query)(
            equalTo(
              BoolQuery(
                must = List(MatchQuery(field = "customer_age", value = 23)),
                should = List(
                  MatchQuery(field = "day_of_week", value = "Monday"),
                  MatchQuery(field = "customer_gender", value = "MALE")
                )
              )
            )
          )
        },
        test("successfully create Exists Query") {
          val query = exists(field = "day_of_week")

          assert(query)(equalTo(ExistsQuery(field = "day_of_week")))
        },
        test("successfully create MatchAll Query") {
          val query = matchAll()

          assert(query)(equalTo(MatchAllQuery()))
        },
        test("successfully create MatchAll Query with boost") {
          val query = matchAll().boost(1.0)

          assert(query)(equalTo(MatchAllQuery(boost = Some(1.0))))
        },
        test("successfully create empty Range Query") {
          val query = range(field = "customer_age")

          assert(query)(equalTo(RangeQuery(field = "customer_age", lower = Unbounded, upper = Unbounded)))
        },
        test("successfully create Range Query with upper bound") {
          val query = range(field = "customer_age").lt(23)

          assert(query)(equalTo(RangeQuery(field = "customer_age", lower = Unbounded, upper = LessThan(23))))
        },
        test("successfully create Range Query with lower bound") {
          val query = range(field = "customer_age").gt(23)

          assert(query)(equalTo(RangeQuery(field = "customer_age", lower = GreaterThan(23), upper = Unbounded)))
        },
        test("successfully create Range Query with inclusive upper bound") {
          val query = range(field = "customer_age").lte(23)

          assert(query)(equalTo(RangeQuery(field = "customer_age", lower = Unbounded, upper = LessThanOrEqualTo(23))))
        },
        test("successfully create Range Query with inclusive lower bound") {
          val query = range(field = "customer_age").gte(23)

          assert(query)(
            equalTo(RangeQuery(field = "customer_age", lower = GreaterThanOrEqualTo(23), upper = Unbounded))
          )
        },
        test("successfully create Range Query with both upper and lower bound") {
          val query = range(field = "customer_age").gte(23).lt(50)

          assert(query)(
            equalTo(RangeQuery(field = "customer_age", lower = GreaterThanOrEqualTo(23), upper = LessThan(50)))
          )
        },
        test("successfully create Term Query") {
          val queryInt    = term(field = "day_of_week", value = 1)
          val queryString = term(field = "day_of_week", value = "Monday")
          val queryBool   = term(field = "day_of_week", value = true)
          val queryLong   = term(field = "day_of_week", value = 1L)

          assert(queryInt)(equalTo(TermQuery(field = "day_of_week", value = 1)))
          assert(queryString)(equalTo(TermQuery(field = "day_of_week", value = "Monday")))
          assert(queryBool)(equalTo(TermQuery(field = "day_of_week", value = true)))
          assert(queryLong)(equalTo(TermQuery(field = "day_of_week", value = 1L)))
        },
        test("successfully create Term Query with boost") {
          val queryInt    = term(field = "day_of_week", value = 1).boost(1.0)
          val queryString = term(field = "day_of_week", value = "Monday").boost(1.0)
          val queryBool   = term(field = "day_of_week", value = true).boost(1.0)
          val queryLong   = term(field = "day_of_week", value = 1L).boost(1.0)

          assert(queryInt)(equalTo(TermQuery(field = "day_of_week", value = 1, boost = Some(1.0))))
          assert(queryString)(equalTo(TermQuery(field = "day_of_week", value = "Monday", boost = Some(1.0))))
          assert(queryBool)(equalTo(TermQuery(field = "day_of_week", value = true, boost = Some(1.0))))
          assert(queryLong)(equalTo(TermQuery(field = "day_of_week", value = 1L, boost = Some(1.0))))
        },
        test("successfully create case insensitive Term Query") {
          val queryString = term(field = "day_of_week", value = "Monday").caseInsensitiveTrue

          assert(queryString)(equalTo(TermQuery(field = "day_of_week", value = "Monday", caseInsensitive = Some(true))))
        },
        test("successfully create case insensitive Term Query with boost") {
          val queryString = term(field = "day_of_week", value = "Monday").boost(1.0).caseInsensitiveTrue

          assert(queryString)(
            equalTo(TermQuery(field = "day_of_week", value = "Monday", boost = Some(1.0), caseInsensitive = Some(true)))
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

          assert(query.toJsonBody)(equalTo(expected.toJson))
        },
        test("properly encode Bool Query with Must containing `Match` leaf query") {
          val query = boolQuery().must(matches(field = "day_of_week", value = "Monday"))
          val expected =
            """
              |{
              |  "query": {
              |    "bool": {
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

          assert(query.toJsonBody)(equalTo(expected.toJson))
        },
        test("properly encode Bool Query with Should containing `Match` leaf query") {
          val query = boolQuery().should(matches(field = "day_of_week", value = "Monday"))
          val expected =
            """
              |{
              |  "query": {
              |    "bool": {
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

          assert(query.toJsonBody)(equalTo(expected.toJson))
        },
        test("properly encode Bool Query with both Must and Should containing `Match` leaf query") {
          val query = boolQuery()
            .must(matches(field = "customer_id", value = 1))
            .should(matches(field = "day_of_week", value = "Monday"))
          val expected =
            """
              |{
              |  "query": {
              |    "bool": {
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

          assert(query.toJsonBody)(equalTo(expected.toJson))
        },
        test("properly encode MatchAll Query") {
          val query = matchAll()
          val expected =
            """
              |{
              |  "query": {
              |    "match_all": {}
              |  }
              |}
              |""".stripMargin

          assert(query.toJsonBody)(equalTo(expected.toJson))
        },
        test("properly encode MatchAll Query with boost") {
          val query = matchAll().boost(1.0)
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

          assert(query.toJsonBody)(equalTo(expected.toJson))
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

          assert(query.toJsonBody)(equalTo(expected.toJson))
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

          assert(query.toJsonBody)(equalTo(expected.toJson))
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

          assert(query.toJsonBody)(equalTo(expected.toJson))
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

          assert(query.toJsonBody)(equalTo(expected.toJson))
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

          assert(query.toJsonBody)(equalTo(expected.toJson))
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

          assert(query.toJsonBody)(equalTo(expected.toJson))
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

          assert(query.toJsonBody)(equalTo(expected.toJson))
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

          assert(query.toJsonBody)(equalTo(expected.toJson))
        },
        test("properly encode Bulk request body") {
          val index = IndexName("users")
          val customer =
            CustomerDocument(id = "WeeMwR5d5", name = "Name", address = "Address", balance = 1000, age = 24)
          val req1 =
            ElasticRequest.create[CustomerDocument](index, DocumentId("ETux1srpww2ObCx"), customer.copy(age = 39))
          val req2 = ElasticRequest.create[CustomerDocument](index, customer)
          val req3 =
            ElasticRequest.upsert[CustomerDocument](index, DocumentId("yMyEG8iFL5qx"), customer.copy(balance = 3000))
          val req4 = ElasticRequest.deleteById(index, DocumentId("1VNzFt2XUFZfXZheDc"))
          val bulkQuery = ElasticRequest.bulk(req1, req2, req3, req4) match {
            case r: BulkRequest => Some(r.body)
            case _              => None
          }

          val expectedBody =
            """|{ "create" : { "_index" : "users", "_id" : "ETux1srpww2ObCx" } }
               |{"id":"WeeMwR5d5","name":"Name","address":"Address","balance":1000,"age":39}
               |{ "create" : { "_index" : "users" } }
               |{"id":"WeeMwR5d5","name":"Name","address":"Address","balance":1000,"age":24}
               |{ "index" : { "_index" : "users", "_id" : "yMyEG8iFL5qx" } }
               |{"id":"WeeMwR5d5","name":"Name","address":"Address","balance":3000,"age":24}
               |{ "delete" : { "_index" : "users", "_id" : "1VNzFt2XUFZfXZheDc" } }
               |""".stripMargin

          assert(bulkQuery)(isSome(equalTo(expectedBody)))
        }
      )
    )
}
