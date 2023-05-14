package zio.elasticsearch

import zio.elasticsearch.ElasticSort._
import zio.elasticsearch.domain._
import zio.elasticsearch.query.sort.Missing._
import zio.elasticsearch.query.sort.NumericType.{Long => NumTypeLong}
import zio.elasticsearch.query.sort.SortMode._
import zio.elasticsearch.query.sort.SortOrder._
import zio.elasticsearch.query.sort.SourceType.NumberType
import zio.elasticsearch.query.sort._
import zio.elasticsearch.script.Script
import zio.elasticsearch.utils._
import zio.json.ast.Json
import zio.json.ast.Json.{Arr, Obj}
import zio.test.Assertion.equalTo
import zio.test._
import zio.{Chunk, Scope}

object SortSpec extends ZIOSpecDefault {
  def spec: Spec[Environment with TestEnvironment with Scope, Any] =
    suite("Sort by")(
      suite("creating SortByField")(
        test("successfully create SortByField with only field given") {
          assert(sortBy("day_of_week"))(
            equalTo(
              SortByFieldOptions(
                field = "day_of_week",
                format = None,
                missing = None,
                mode = None,
                numericType = None,
                order = None,
                unmappedType = None
              )
            )
          )
        },
        test("successfully create type-safe SortByField with only field given") {
          assert(sortBy(TestDocument.intField))(
            equalTo(
              SortByFieldOptions(
                field = "intField",
                format = None,
                missing = None,
                mode = None,
                numericType = None,
                order = None,
                unmappedType = None
              )
            )
          )
        },
        test("successfully create type-safe SortByField with given `format`") {
          assert(sortBy(TestDocument.dateField).format("strict_date_optional_time_nanos"))(
            equalTo(
              SortByFieldOptions(
                field = "dateField",
                format = Some("strict_date_optional_time_nanos"),
                missing = None,
                mode = None,
                numericType = None,
                order = None,
                unmappedType = None
              )
            )
          )
        },
        test("successfully create type-safe SortByField with given `missing`") {
          assert(sortBy(TestDocument.intField).missing(First))(
            equalTo(
              SortByFieldOptions(
                field = "intField",
                format = None,
                missing = Some(First),
                mode = None,
                numericType = None,
                order = None,
                unmappedType = None
              )
            )
          )
        },
        test("successfully create type-safe SortByField with given `mode`") {
          assert(sortBy(TestSubDocument.intFieldList).mode(Avg))(
            equalTo(
              SortByFieldOptions(
                field = "intFieldList",
                format = None,
                missing = None,
                mode = Some(Avg),
                numericType = None,
                order = None,
                unmappedType = None
              )
            )
          )
        },
        test("successfully create type-safe SortByField with given `numericType`") {
          assert(sortBy(TestDocument.intField).numericType(NumTypeLong))(
            equalTo(
              SortByFieldOptions(
                field = "intField",
                format = None,
                missing = None,
                mode = None,
                numericType = Some(NumTypeLong),
                order = None,
                unmappedType = None
              )
            )
          )
        },
        test("successfully create type-safe SortByField with given `order`") {
          assert(sortBy(TestDocument.intField).order(Desc))(
            equalTo(
              SortByFieldOptions(
                field = "intField",
                format = None,
                missing = None,
                mode = None,
                numericType = None,
                order = Some(Desc),
                unmappedType = None
              )
            )
          )
        },
        test("successfully create type-safe SortByField with given `unmappedType`") {
          assert(sortBy(TestDocument.intField).unmappedType("long"))(
            equalTo(
              SortByFieldOptions(
                field = "intField",
                format = None,
                missing = None,
                mode = None,
                numericType = None,
                order = None,
                unmappedType = Some("long")
              )
            )
          )
        },
        test("successfully create type-safe SortByField with given all params") {
          assert(
            sortBy(TestDocument.dateField)
              .format("strict_date_optional_time_nanos")
              .missing(First)
              .mode(Avg)
              .numericType(NumTypeLong)
              .order(Desc)
              .unmappedType("long")
          )(
            equalTo(
              SortByFieldOptions(
                field = "dateField",
                format = Some("strict_date_optional_time_nanos"),
                missing = Some(First),
                mode = Some(Avg),
                numericType = Some(NumTypeLong),
                order = Some(Desc),
                unmappedType = Some("long")
              )
            )
          )
        },
        test("successfully create SortByScript without additional fields") {
          assert(sortBy(script = Script("doc['day_of_week'].value"), sourceType = NumberType))(
            equalTo(
              SortByScriptOptions(
                script = Script(source = "doc['day_of_week'].value", params = Map.empty, lang = None),
                sourceType = NumberType,
                mode = None,
                order = None
              )
            )
          )
        },
        test("successfully create SortByScript with given `mode`") {
          assert(
            sortBy(Script(source = "doc['day_of_week'].value * params['factor']").params("factor" -> 2), NumberType)
              .mode(Avg)
          )(
            equalTo(
              SortByScriptOptions(
                script = Script(
                  source = "doc['day_of_week'].value * params['factor']",
                  params = Map("factor" -> 2),
                  lang = None
                ),
                sourceType = NumberType,
                mode = Some(Avg),
                order = None
              )
            )
          )
        },
        test("successfully create SortByScript with given `order`") {
          assert(sortBy(Script(source = "doc['day_of_week'].value").lang("painless"), NumberType).order(Desc))(
            equalTo(
              SortByScriptOptions(
                script = Script(source = "doc['day_of_week'].value", params = Map.empty, lang = Some("painless")),
                sourceType = NumberType,
                mode = None,
                order = Some(Desc)
              )
            )
          )
        },
        test("successfully create SortByScript with given `mode` and `order`") {
          assert(
            sortBy(
              Script(source = "doc['day_of_week'].value * params['factor']").params("factor" -> 2).lang("painless"),
              NumberType
            )
              .mode(Avg)
              .order(Asc)
          )(
            equalTo(
              SortByScriptOptions(
                script = Script(
                  source = "doc['day_of_week'].value * params['factor']",
                  params = Map("factor" -> 2),
                  lang = Some("painless")
                ),
                sourceType = NumberType,
                mode = Some(Avg),
                order = Some(Asc)
              )
            )
          )
        }
      ),
      suite("encoding SortBy as JSON")(
        test("properly encode SortByField with only field") {
          val sort = sortBy(TestDocument.intField)
          val expected =
            """
              |{
              |  "sort": [
              |    "intField"
              |  ]
              |}
              |""".stripMargin

          assert(sortsToJson(sort))(equalTo(expected.toJson))
        },
        test("properly encode SortByField with `format` given") {
          val sort = sortBy(TestDocument.dateField).format("strict_date_optional_time_nanos")
          val expected =
            """
              |{
              |  "sort": [
              |    {
              |      "dateField": {
              |        "format": "strict_date_optional_time_nanos"
              |      }
              |    }
              |  ]
              |}
              |""".stripMargin

          assert(sortsToJson(sort))(equalTo(expected.toJson))
        },
        test("properly encode SortByField with `missing` given") {
          val sort = sortBy(TestDocument.intField).missing(First)
          val expected =
            """
              |{
              |  "sort": [
              |    {
              |      "intField": {
              |        "missing": "_first"
              |      }
              |    }
              |  ]
              |}
              |""".stripMargin

          assert(sortsToJson(sort))(equalTo(expected.toJson))
        },
        test("properly encode SortByField with `mode` given") {
          val sort = sortBy(TestSubDocument.intFieldList).mode(Avg)
          val expected =
            """
              |{
              |  "sort": [
              |    {
              |      "intFieldList": {
              |        "mode": "avg"
              |      }
              |    }
              |  ]
              |}
              |""".stripMargin

          assert(sortsToJson(sort))(equalTo(expected.toJson))
        },
        test("properly encode SortByField with `numericType` given") {
          val sort = sortBy(TestDocument.intField).numericType(NumTypeLong)
          val expected =
            """
              |{
              |  "sort": [
              |    {
              |      "intField": {
              |        "numeric_type": "long"
              |      }
              |    }
              |  ]
              |}
              |""".stripMargin

          assert(sortsToJson(sort))(equalTo(expected.toJson))
        },
        test("properly encode SortByField with `order` given") {
          val sort = sortBy(TestDocument.intField).order(Desc)
          val expected =
            """
              |{
              |  "sort": [
              |    {
              |      "intField": {
              |        "order": "desc"
              |      }
              |    }
              |  ]
              |}
              |""".stripMargin

          assert(sortsToJson(sort))(equalTo(expected.toJson))
        },
        test("properly encode SortByField with `unmappedType` given") {
          val sort = sortBy(TestDocument.intField).unmappedType("long")
          val expected =
            """
              |{
              |  "sort": [
              |    {
              |      "intField": {
              |        "unmapped_type": "long"
              |      }
              |    }
              |  ]
              |}
              |""".stripMargin

          assert(sortsToJson(sort))(equalTo(expected.toJson))
        },
        test("properly encode SortByField with all params given") {
          val sort = sortBy(TestDocument.dateField)
            .format("strict_date_optional_time_nanos")
            .missing(First)
            .mode(Avg)
            .numericType(NumTypeLong)
            .order(Desc)
            .unmappedType("long")
          val expected =
            """
              |{
              |  "sort": [
              |    {
              |      "dateField": {
              |        "format": "strict_date_optional_time_nanos", "missing": "_first", "mode": "avg", "numeric_type": "long", "order": "desc", "unmapped_type": "long"
              |      }
              |    }
              |  ]
              |}
              |""".stripMargin

          assert(sortsToJson(sort))(equalTo(expected.toJson))
        },
        test("properly encode multiple SortByField") {
          val sort1 = sortBy(TestDocument.intField).order(Desc)
          val sort2 = sortBy("day_of_month").missing(First)
          val expected =
            """
              |{
              |  "sort": [
              |    {
              |      "intField": {
              |        "order": "desc"
              |      }
              |    },
              |    {
              |      "day_of_month": {
              |        "missing": "_first"
              |      }
              |    }
              |  ]
              |}
              |""".stripMargin

          assert(sortsToJson(sort1, sort2))(equalTo(expected.toJson))
        },
        test("properly encode SortByScript without additional params") {
          val sort = sortBy(script = Script("doc['day_of_week'].value"), sourceType = NumberType)
          val expected =
            """
              |{
              |  "sort": [
              |    {
              |      "_script": {
              |        "type": "number",
              |        "script": {
              |          "source": "doc['day_of_week'].value"
              |        }
              |      }
              |    }
              |  ]
              |}
              |""".stripMargin

          assert(sortsToJson(sort))(equalTo(expected.toJson))
        },
        test("properly encode SortByScript with given `mode`") {
          val sort = sortBy(
            script = Script("doc['day_of_week'].value * params['factor']").params("factor" -> 2),
            sourceType = NumberType
          )
            .mode(Avg)
          val expected =
            """
              |{
              |  "sort": [
              |    {
              |      "_script": {
              |        "type": "number",
              |        "script": {
              |          "source": "doc['day_of_week'].value * params['factor']",
              |          "params": {
              |            "factor": 2
              |          }
              |        },
              |        "mode": "avg"
              |      }
              |    }
              |  ]
              |}
              |""".stripMargin

          assert(sortsToJson(sort))(equalTo(expected.toJson))
        },
        test("properly encode SortByScript with given `order`") {
          val sort =
            sortBy(script = Script("doc['day_of_week'].value").lang("painless"), sourceType = NumberType).order(Desc)
          val expected =
            """
              |{
              |  "sort": [
              |    {
              |      "_script": {
              |        "type": "number",
              |        "script": {
              |          "lang": "painless",
              |          "source": "doc['day_of_week'].value"
              |        },
              |        "order": "desc"
              |      }
              |    }
              |  ]
              |}
              |""".stripMargin

          assert(sortsToJson(sort))(equalTo(expected.toJson))
        },
        test("properly encode SortByScript with `mode` and `order`") {
          val sort = sortBy(
            Script(source = "doc['day_of_week'].value * params['factor']").params("factor" -> 2).lang("painless"),
            NumberType
          )
            .mode(Avg)
            .order(Asc)
          val expected =
            """
              |{
              |  "sort": [
              |    {
              |      "_script": {
              |        "type": "number",
              |        "script": {
              |          "lang": "painless",
              |          "source": "doc['day_of_week'].value * params['factor']",
              |          "params": {
              |            "factor": 2
              |          }
              |        },
              |        "mode": "avg",
              |        "order": "asc"
              |      }
              |    }
              |  ]
              |}
              |""".stripMargin

          assert(sortsToJson(sort))(equalTo(expected.toJson))
        },
        test("properly encode SortByField and SortByScript") {
          val sort1 = sortBy("day_of_month").order(Desc)
          val sort2 = sortBy(Script(source = "doc['day_of_week'].value").lang("painless"), NumberType).order(Asc)
          val expected =
            """
              |{
              |  "sort": [
              |    {
              |      "day_of_month": {
              |        "order": "desc"
              |      }
              |    },
              |    {
              |      "_script": {
              |        "type": "number",
              |        "script": {
              |          "lang": "painless",
              |          "source": "doc['day_of_week'].value"
              |        },
              |        "order": "asc"
              |      }
              |    }
              |  ]
              |}
              |""".stripMargin

          assert(sortsToJson(sort1, sort2))(equalTo(expected.toJson))
        }
      )
    )

  private def sortsToJson(sorts: Sort*): Json = Obj("sort" -> Arr(Chunk.fromIterable(sorts).map(_.paramsToJson)))
}
