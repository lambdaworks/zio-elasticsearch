package zio.elasticsearch

import zio.Scope
import zio.elasticsearch.ElasticSort._
import zio.elasticsearch.query.sort.Missing._
import zio.elasticsearch.query.sort.NumericType.{Long => NumTypeLong}
import zio.elasticsearch.query.sort.SortMode._
import zio.elasticsearch.query.sort.SortOrder._
import zio.elasticsearch.query.sort.SourceType.NumberType
import zio.elasticsearch.query.sort._
import zio.elasticsearch.utils._
import zio.json.ast.Json
import zio.json.ast.Json.{Arr, Obj}
import zio.test.Assertion.equalTo
import zio.test._

object SortSpec extends ZIOSpecDefault {
  def spec: Spec[Environment with TestEnvironment with Scope, Any] =
    suite("Sort by")(
      suite("creating SortByField")(
        test("successfully create SortByField with only field given") {
          assert(sortByField("day_of_week"))(
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
        test("successfully create SortByField with only type-safe field given") {
          assert(sortByField(UserDocument.age))(
            equalTo(
              SortByFieldOptions(
                field = "age",
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
        test("successfully create SortByField with given `format`") {
          assert(sortByField("day_of_week").format("strict_date_optional_time_nanos"))(
            equalTo(
              SortByFieldOptions(
                field = "day_of_week",
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
        test("successfully create SortByField with given `missing`") {
          assert(sortByField("day_of_week").missing(First))(
            equalTo(
              SortByFieldOptions(
                field = "day_of_week",
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
        test("successfully create SortByField with given `mode`") {
          assert(sortByField("day_of_week").mode(Avg))(
            equalTo(
              SortByFieldOptions(
                field = "day_of_week",
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
        test("successfully create SortByField with given `numericType`") {
          assert(sortByField("day_of_week").numericType(NumTypeLong))(
            equalTo(
              SortByFieldOptions(
                field = "day_of_week",
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
        test("successfully create SortByField with given `order`") {
          assert(sortByField("day_of_week").order(Desc))(
            equalTo(
              SortByFieldOptions(
                field = "day_of_week",
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
        test("successfully create SortByField with given `unmappedType`") {
          assert(sortByField("day_of_week").unmappedType("long"))(
            equalTo(
              SortByFieldOptions(
                field = "day_of_week",
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
        test("successfully create SortByField with given all params") {
          assert(
            sortByField("day_of_week")
              .format("strict_date_optional_time_nanos")
              .missing(First)
              .mode(Avg)
              .numericType(NumTypeLong)
              .order(Desc)
              .unmappedType("long")
          )(
            equalTo(
              SortByFieldOptions(
                field = "day_of_week",
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
          assert(sortByScript("doc['day_of_week'].value", NumberType))(
            equalTo(
              SortByScriptOptions(
                params = Map.empty,
                source = "doc['day_of_week'].value",
                sourceType = NumberType,
                lang = None,
                mode = None,
                order = None
              )
            )
          )
        },
        test("successfully create SortByScript with given `params`") {
          assert(sortByScript("doc['day_of_week'].value * params['factor']", NumberType).withParam("factor" -> 2))(
            equalTo(
              SortByScriptOptions(
                params = Map("factor" -> 2),
                source = "doc['day_of_week'].value * params['factor']",
                sourceType = NumberType,
                lang = None,
                mode = None,
                order = None
              )
            )
          )
        },
        test("successfully create SortByScript with given `lang`") {
          assert(sortByScript("doc['day_of_week'].value", NumberType).lang("painless"))(
            equalTo(
              SortByScriptOptions(
                params = Map.empty,
                source = "doc['day_of_week'].value",
                sourceType = NumberType,
                lang = Some("painless"),
                mode = None,
                order = None
              )
            )
          )
        },
        test("successfully create SortByScript with given `mode`") {
          assert(sortByScript("doc['day_of_week'].value", NumberType).mode(Avg))(
            equalTo(
              SortByScriptOptions(
                params = Map.empty,
                source = "doc['day_of_week'].value",
                sourceType = NumberType,
                lang = None,
                mode = Some(Avg),
                order = None
              )
            )
          )
        },
        test("successfully create SortByScript with given `order`") {
          assert(sortByScript("doc['day_of_week'].value", NumberType).order(Desc))(
            equalTo(
              SortByScriptOptions(
                params = Map.empty,
                source = "doc['day_of_week'].value",
                sourceType = NumberType,
                lang = None,
                mode = None,
                order = Some(Desc)
              )
            )
          )
        },
        test("successfully create SortByScript with given `params`, `lang`, `mode` and `order`") {
          assert(
            sortByScript("doc['day_of_week'].value * params['factor']", NumberType)
              .withParam("factor" -> 2)
              .lang("painless")
              .mode(Avg)
              .order(Asc)
          )(
            equalTo(
              SortByScriptOptions(
                params = Map("factor" -> 2),
                source = "doc['day_of_week'].value * params['factor']",
                sourceType = NumberType,
                lang = Some("painless"),
                mode = Some(Avg),
                order = Some(Asc)
              )
            )
          )
        }
      ),
      suite("encoding SortBy as JSON")(
        test("properly encode SortByField with only field") {
          val sort = sortByField("day_of_week")
          val expected =
            """
              |{
              |  "sort": [
              |    {
              |      "day_of_week": {}
              |    }
              |  ]
              |}
              |""".stripMargin

          assert(sortsToJson(sort))(equalTo(expected.toJson))
        },
        test("properly encode SortByField with `format` given") {
          val sort = sortByField("day_of_week").format("strict_date_optional_time_nanos")
          val expected =
            """
              |{
              |  "sort": [
              |    {
              |      "day_of_week": {
              |        "format": "strict_date_optional_time_nanos"
              |      }
              |    }
              |  ]
              |}
              |""".stripMargin

          assert(sortsToJson(sort))(equalTo(expected.toJson))
        },
        test("properly encode SortByField with `missing` given") {
          val sort = sortByField("day_of_week").missing(First)
          val expected =
            """
              |{
              |  "sort": [
              |    {
              |      "day_of_week": {
              |        "missing": "_first"
              |      }
              |    }
              |  ]
              |}
              |""".stripMargin

          assert(sortsToJson(sort))(equalTo(expected.toJson))
        },
        test("properly encode SortByField with `mode` given") {
          val sort = sortByField("day_of_week").mode(Avg)
          val expected =
            """
              |{
              |  "sort": [
              |    {
              |      "day_of_week": {
              |        "mode": "avg"
              |      }
              |    }
              |  ]
              |}
              |""".stripMargin

          assert(sortsToJson(sort))(equalTo(expected.toJson))
        },
        test("properly encode SortByField with `numericType` given") {
          val sort = sortByField("day_of_week").numericType(NumTypeLong)
          val expected =
            """
              |{
              |  "sort": [
              |    {
              |      "day_of_week": {
              |        "numeric_type": "long"
              |      }
              |    }
              |  ]
              |}
              |""".stripMargin

          assert(sortsToJson(sort))(equalTo(expected.toJson))
        },
        test("properly encode SortByField with `order` given") {
          val sort = sortByField("day_of_week").order(Desc)
          val expected =
            """
              |{
              |  "sort": [
              |    {
              |      "day_of_week": {
              |        "order": "desc"
              |      }
              |    }
              |  ]
              |}
              |""".stripMargin

          assert(sortsToJson(sort))(equalTo(expected.toJson))
        },
        test("properly encode SortByField with `unmappedType` given") {
          val sort = sortByField("day_of_week").unmappedType("long")
          val expected =
            """
              |{
              |  "sort": [
              |    {
              |      "day_of_week": {
              |        "unmapped_type": "long"
              |      }
              |    }
              |  ]
              |}
              |""".stripMargin

          assert(sortsToJson(sort))(equalTo(expected.toJson))
        },
        test("properly encode SortByField with all params given") {
          val sort = sortByField("day_of_week")
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
              |      "day_of_week": {
              |        "format": "strict_date_optional_time_nanos", "missing": "_first", "mode": "avg", "numeric_type": "long", "order": "desc", "unmapped_type": "long"
              |      }
              |    }
              |  ]
              |}
              |""".stripMargin

          assert(sortsToJson(sort))(equalTo(expected.toJson))
        },
        test("properly encode multiple SortByField") {
          val sort1 = sortByField("day_of_week").order(Desc)
          val sort2 = sortByField("day_of_month").missing(First)
          val expected =
            """
              |{
              |  "sort": [
              |    {
              |      "day_of_week": {
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
          val sort = sortByScript("doc['day_of_week'].value", NumberType)
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
        test("properly encode SortByScript with given `params`") {
          val sort = sortByScript("doc['day_of_week'].value * params['factor']", NumberType).withParam("factor" -> 2)
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
              |        }
              |      }
              |    }
              |  ]
              |}
              |""".stripMargin

          assert(sortsToJson(sort))(equalTo(expected.toJson))
        },
        test("properly encode SortByScript with given `lang`") {
          val sort = sortByScript("doc['day_of_week'].value", NumberType).lang("painless")
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
              |        }
              |      }
              |    }
              |  ]
              |}
              |""".stripMargin

          assert(sortsToJson(sort))(equalTo(expected.toJson))
        },
        test("properly encode SortByScript with given `mode`") {
          val sort = sortByScript("doc['day_of_week'].value", NumberType).mode(Avg)
          val expected =
            """
              |{
              |  "sort": [
              |    {
              |      "_script": {
              |        "type": "number",
              |        "script": {
              |          "source": "doc['day_of_week'].value"
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
          val sort = sortByScript("doc['day_of_week'].value", NumberType).order(Desc)
          val expected =
            """
              |{
              |  "sort": [
              |    {
              |      "_script": {
              |        "type": "number",
              |        "script": {
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
        test("properly encode SortByScript with given `params`, `lang`, `mode` and `order`") {
          val sort = sortByScript("doc['day_of_week'].value * params['factor']", NumberType)
            .withParam("factor" -> 2)
            .lang("painless")
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
          val sort1 = sortByField("day_of_month").order(Desc)
          val sort2 = sortByScript("doc['day_of_week'].value", NumberType).lang("painless").order(Asc)
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

  private def sortsToJson(sorts: Sort*): Json = Obj("sort" -> Arr(sorts.map(_.paramsToJson): _*))
}
