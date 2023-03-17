package zio.elasticsearch

import zio.Scope
import zio.elasticsearch.Missing.First
import zio.elasticsearch.Mode.Avg
import zio.elasticsearch.Order.Desc
import zio.elasticsearch.SortBy.{SortByData, sortBy}
import zio.elasticsearch.NumericType.{Long => NumTypeLong}
import zio.elasticsearch.utils._
import zio.json.ast.Json
import zio.json.ast.Json.{Arr, Obj}
import zio.test.Assertion.equalTo
import zio.test._

object SortBySpec extends ZIOSpecDefault {
  def spec: Spec[Environment with TestEnvironment with Scope, Any] =
    suite("Sort by")(
      suite("creating SortBy")(
        test("successfully create SortBy with only field given") {
          assert(sortBy("day_of_week"))(
            equalTo(
              SortByData(
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
        test("successfully create SortBy with given `format`") {
          assert(sortBy("day_of_week").format("strict_date_optional_time_nanos"))(
            equalTo(
              SortByData(
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
        test("successfully create SortBy with given `missing`") {
          assert(sortBy("day_of_week").missing(First))(
            equalTo(
              SortByData(
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
        test("successfully create SortBy with given `mode`") {
          assert(sortBy("day_of_week").mode(Avg))(
            equalTo(
              SortByData(
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
        test("successfully create SortBy with given `numericType`") {
          assert(sortBy("day_of_week").numericType(NumTypeLong))(
            equalTo(
              SortByData(
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
        test("successfully create SortBy with given `order`") {
          assert(sortBy("day_of_week").order(Desc))(
            equalTo(
              SortByData(
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
        test("successfully create SortBy with given `unmappedType`") {
          assert(sortBy("day_of_week").unmappedType("long"))(
            equalTo(
              SortByData(
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
        test("successfully create SortBy with given all params") {
          assert(
            sortBy("day_of_week")
              .format("strict_date_optional_time_nanos")
              .missing(First)
              .mode(Avg)
              .numericType(NumTypeLong)
              .order(Desc)
              .unmappedType("long")
          )(
            equalTo(
              SortByData(
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
        }
      ),
      suite("encoding SortBy as JSON")(
        test("properly encode SortBy with only field") {
          val sort = sortBy("day_of_week")
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
        test("properly encode SortBy with `format` given") {
          val sort = sortBy("day_of_week").format("strict_date_optional_time_nanos")
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
        test("properly encode SortBy with `missing` given") {
          val sort = sortBy("day_of_week").missing(First)
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
        test("properly encode SortBy with `mode` given") {
          val sort = sortBy("day_of_week").mode(Avg)
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
        test("properly encode SortBy with `numericType` given") {
          val sort = sortBy("day_of_week").numericType(NumTypeLong)
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
        test("properly encode SortBy with `order` given") {
          val sort = sortBy("day_of_week").order(Desc)
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
        test("properly encode SortBy with `unmappedType` given") {
          val sort = sortBy("day_of_week").unmappedType("long")
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
        test("properly encode SortBy with all params given") {
          val sort = sortBy("day_of_week")
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
        test("properly encode multiple SortBy") {
          val sort1 = sortBy("day_of_week").order(Desc)
          val sort2 = sortBy("day_of_month").missing(First)
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
        }
      )
    )
  private def sortsToJson(sorts: SortBy*): Json = Obj("sort" -> Arr(sorts.map(_.paramsToJson): _*))
}
