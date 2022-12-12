package zio.elasticsearch

import zio.Scope
import zio.elasticsearch.ElasticQuery._
import zio.json.ast.Json
import zio.test.{Assertion, Spec, TestEnvironment, ZIOSpecDefault, assert}

object QueryDSLSpec extends ZIOSpecDefault {

  override def spec: Spec[Environment with TestEnvironment with Scope, Any] =
    suite("Query DSL")(
      suite("Creating Elastic Query Class")(
        test("Successfully create Match Query using `matches` method") {
          val queryString = matches("day_of_week", "Monday")
          val queryBool   = matches("day_of_week", true)
          val queryLong   = matches("day_of_week", 1L)

          assert(queryString)(Assertion.equalTo(Match("day_of_week", "Monday")))
          assert(queryBool)(Assertion.equalTo(Match("day_of_week", true)))
          assert(queryLong)(Assertion.equalTo(Match("day_of_week", 1)))
        },
        test("Successfully create `Must` Query from two Match queries") {
          val query = boolQuery().must(matches("day_of_week", "Monday"), matches("customer_gender", "MALE"))

          assert(query)(
            Assertion.equalTo(
              BoolQuery(List(Match("day_of_week", "Monday"), Match("customer_gender", "MALE")), List.empty)
            )
          )
        },
        test("Successfully create `Should` Query from two Match queries") {
          val query = boolQuery().should(matches("day_of_week", "Monday"), matches("customer_gender", "MALE"))

          assert(query)(
            Assertion.equalTo(
              BoolQuery(List.empty, List(Match("day_of_week", "Monday"), Match("customer_gender", "MALE")))
            )
          )
        },
        test("Successfully create `Must/Should` mixed Query") {
          val query = boolQuery()
            .must(matches("day_of_week", "Monday"), matches("customer_gender", "MALE"))
            .should(matches("customer_age", 23))

          assert(query)(
            Assertion.equalTo(
              BoolQuery(
                List(Match("day_of_week", "Monday"), Match("customer_gender", "MALE")),
                List(Match("customer_age", 23))
              )
            )
          )
        },
        test("Successfully create `Should/Must` mixed Query") {
          val query = boolQuery()
            .must(matches("customer_age", 23))
            .should(matches("day_of_week", "Monday"), matches("customer_gender", "MALE"))

          assert(query)(
            Assertion.equalTo(
              BoolQuery(
                List(Match("customer_age", 23)),
                List(Match("day_of_week", "Monday"), Match("customer_gender", "MALE"))
              )
            )
          )
        }
      ),
      suite("Writing out Elastic Query as Json")(
        test("Properly write JSON body for Match query") {
          val queryBool = matches("day_of_week", true)

          assert(queryBool.asJsonBody)(
            Assertion.equalTo(
              Json.Obj("query" -> Json.Obj("match" -> Json.Obj("day_of_week" -> Json.Bool(true))))
            )
          )
        },
        test("Properly write JSON body for must query") {
          val queryBool = boolQuery().must(matches("day_of_week", "Monday"))

          assert(queryBool.asJsonBody)(
            Assertion.equalTo(
              Json.Obj(
                "query" -> Json.Obj(
                  "bool" -> Json.Obj(
                    "must" -> Json.Arr(
                      Json.Obj("match" -> Json.Obj("day_of_week" -> Json.Str("Monday")))
                    ),
                    "should" -> Json.Arr()
                  )
                )
              )
            )
          )
        },
        test("Properly write JSON body for must query") {
          val queryBool = boolQuery().should(matches("day_of_week", "Monday"))

          assert(queryBool.asJsonBody)(
            Assertion.equalTo(
              Json.Obj(
                "query" -> Json.Obj(
                  "bool" -> Json.Obj(
                    "must"   -> Json.Arr(),
                    "should" -> Json.Arr(Json.Obj("match" -> Json.Obj("day_of_week" -> Json.Str("Monday"))))
                  )
                )
              )
            )
          )
        },
        test("Properly write JSON body for mixed `AND/OR` query") {
          val queryBool =
            boolQuery()
              .must(matches("customer_id", 1))
              .should(matches("day_of_week", "Monday"))

          assert(queryBool.asJsonBody)(
            Assertion.equalTo(
              Json.Obj(
                "query" -> Json.Obj(
                  "bool" -> Json.Obj(
                    "must"   -> Json.Arr(Json.Obj("match" -> Json.Obj("customer_id" -> Json.Num(1)))),
                    "should" -> Json.Arr(Json.Obj("match" -> Json.Obj("day_of_week" -> Json.Str("Monday"))))
                  )
                )
              )
            )
          )
        }
      )
    )
}
