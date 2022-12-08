package zio.elasticsearch

import zio.elasticsearch.ElasticQuery._
import zio.json.ast.Json
import zio.json.ast.Json.{Arr, Obj}
import zio.test._

object QueryDSLSpec extends ZIOSpecDefault {

  override def spec =
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
        test("Successfully create `AND` Query from two Match queries") {

          val query = matches("day_of_week", "Monday") and matches("customer_gender", "MALE")

          assert(query)(Assertion.equalTo(And(Match("day_of_week", "Monday"), Match("customer_gender", "MALE"))))
        },
        test("Successfully create `OR` Query from two Match queries") {

          val query = matches("day_of_week", "Monday") or matches("customer_gender", "MALE")

          assert(query)(Assertion.equalTo(Or(Match("day_of_week", "Monday"), Match("customer_gender", "MALE"))))
        },
        test("Successfully create `AND/OR` mixed Query") {

          val query =
            matches("day_of_week", "Monday") and matches("customer_gender", "MALE") or matches("customer_age", 23)

          assert(query)(
            Assertion.equalTo(
              Or(And(Match("day_of_week", "Monday"), Match("customer_gender", "MALE")), Match("customer_age", 23))
            )
          )
        },
        test("Successfully create `OR/AND` mixed Query") {

          val query =
            matches("day_of_week", "Monday") or matches("customer_gender", "MALE") and matches("customer_age", 23)

          assert(query)(
            Assertion.equalTo(
              And(Or(Match("day_of_week", "Monday"), Match("customer_gender", "MALE")), Match("customer_age", 23))
            )
          )
        }
      ),
      suite("Writing out Elastic Query as Json")(
        test("Properly write JSON body for Match query") {
          val queryString = matches("day_of_week", "Monday").asJsonBody

          assert(queryString)(
            Assertion.equalTo(Obj("query" -> Obj("match" -> Obj("day_of_week" -> Json.Str("Monday")))))
          )
        },
        test("Properly write JSON body for `AND` query") {
          val query = matches("day_of_week", "Monday") and matches("customer_gender", 1)

          assert(query.asJsonBody)(
            Assertion.equalTo(
              Obj(
                "query" -> Obj(
                  "bool" -> Obj(
                    "must" -> Arr(
                      Obj(
                        "match" -> Obj(
                          "day_of_week" -> Json.Str("Monday")
                        )
                      ),
                      Obj(
                        "match" -> Obj(
                          "customer_gender" -> Json.Num(1)
                        )
                      )
                    )
                  )
                )
              )
            )
          )
        },
        test("Properly write JSON body for `OR` query") {
          val query = matches("day_of_week", "Monday") or matches("customer_gender", 1)

          assert(query.asJsonBody)(
            Assertion.equalTo(
              Obj(
                "query" -> Obj(
                  "bool" -> Obj(
                    "should" -> Arr(
                      Obj(
                        "match" -> Obj(
                          "day_of_week" -> Json.Str("Monday")
                        )
                      ),
                      Obj(
                        "match" -> Obj(
                          "customer_gender" -> Json.Num(1)
                        )
                      )
                    )
                  )
                )
              )
            )
          )
        },
        test("Properly write JSON body for mixed `AND/OR` query") {
          val query =
            matches("day_of_week", "Monday") and
              matches("customer_gender", 1) or
              matches("isRegular", false)

          assert(query.asJsonBody)(
            Assertion.equalTo(
              Obj(
                "query" -> Obj(
                  "bool" -> Obj(
                    "should" -> Arr(
                      Obj(
                        "bool" -> Obj(
                          "must" -> Arr(
                            Obj(
                              "match" -> Obj(
                                "day_of_week" -> Json.Str("Monday")
                              )
                            ),
                            Obj(
                              "match" -> Obj(
                                "customer_gender" -> Json.Num(1)
                              )
                            )
                          )
                        )
                      ),
                      Obj(
                        "match" -> Obj(
                          "isRegular" -> Json.Bool(false)
                        )
                      )
                    )
                  )
                )
              )
            )
          )
        }
      )
    )

}
