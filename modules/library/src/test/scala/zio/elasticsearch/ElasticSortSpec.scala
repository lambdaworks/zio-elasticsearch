package zio.elasticsearch

import zio.Scope
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
import zio.test.Assertion.equalTo
import zio.test._

object ElasticSortSpec extends ZIOSpecDefault {
  def spec: Spec[Environment with TestEnvironment with Scope, Any] = {
    suite("ElasticSort")(
      suite("constructing")(
        test("sortByField") {
          val sort                   = sortBy("day_of_week")
          val sortTs                 = sortBy(TestDocument.intField)
          val sortTsWithFormat       = sortBy(TestDocument.dateField).format("strict_date_optional_time_nanos")
          val sortTsWithMissing      = sortBy(TestDocument.intField).missing(First)
          val sortTsWithMode         = sortBy(TestSubDocument.intFieldList).mode(Avg)
          val sortTsWithNumericType  = sortBy(TestDocument.intField).numericType(NumTypeLong)
          val sortTsWithOrder        = sortBy(TestDocument.intField).order(Desc)
          val sortTsWithUnmappedType = sortBy(TestDocument.intField).unmappedType("long")
          val sortTsWithAllParams = sortBy(TestDocument.dateField)
            .format("strict_date_optional_time_nanos")
            .missing(First)
            .mode(Avg)
            .numericType(NumTypeLong)
            .order(Desc)
            .unmappedType("long")
          val sortByCount = SortByField.byCount
          val sortByDoc   = SortByField.byDoc
          val sortByKey   = SortByField.byKey
          val sortByScore = SortByField.byScore

          assert(sort)(
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
          ) &&
          assert(sortTs)(
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
          ) && assert(sortTsWithFormat)(
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
          ) && assert(sortTsWithMissing)(
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
          ) && assert(sortTsWithMode)(
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
          ) && assert(sortTsWithNumericType)(
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
          ) && assert(sortTsWithOrder)(
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
          ) && assert(sortTsWithUnmappedType)(
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
          ) && assert(sortTsWithAllParams)(
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
          ) && assert(sortByCount)(
            equalTo(
              SortByFieldOptions(
                field = "_count",
                format = None,
                missing = None,
                mode = None,
                numericType = None,
                order = None,
                unmappedType = None
              )
            )
          ) && assert(sortByDoc)(
            equalTo(
              SortByFieldOptions(
                field = "_doc",
                format = None,
                missing = None,
                mode = None,
                numericType = None,
                order = None,
                unmappedType = None
              )
            )
          ) && assert(sortByKey)(
            equalTo(
              SortByFieldOptions(
                field = "_key",
                format = None,
                missing = None,
                mode = None,
                numericType = None,
                order = None,
                unmappedType = None
              )
            )
          ) && assert(sortByScore)(
            equalTo(
              SortByFieldOptions(
                field = "_score",
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
        test("sortByScript") {
          val sort = sortBy(script = Script.from("doc['day_of_week'].value"), sourceType = NumberType)
          val sortWithMode =
            sortBy(Script.from("doc['day_of_week'].value * params['factor']").params("factor" -> 2), NumberType)
              .mode(Avg)
          val sortWithOrder =
            sortBy(Script.from("doc['day_of_week'].value").lang("painless"), NumberType).order(Desc)
          val sortWithModeAndOrder = sortBy(
            Script.from("doc['day_of_week'].value * params['factor']").params("factor" -> 2).lang("painless"),
            NumberType
          ).mode(Avg).order(Asc)

          assert(sort)(
            equalTo(
              SortByScriptOptions(
                script = Script(source = "doc['day_of_week'].value", params = Map.empty, lang = None),
                sourceType = NumberType,
                mode = None,
                order = None
              )
            )
          ) && assert(sortWithMode)(
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
          ) && assert(sortWithOrder)(
            equalTo(
              SortByScriptOptions(
                script = Script(source = "doc['day_of_week'].value", params = Map.empty, lang = Some("painless")),
                sourceType = NumberType,
                mode = None,
                order = Some(Desc)
              )
            )
          ) && assert(sortWithModeAndOrder)(
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
      suite("encoding as JSON")(
        test("sortByField") {
          val sort                 = sortBy(TestDocument.intField)
          val sortWithFormat       = sortBy(TestDocument.dateField).format("strict_date_optional_time_nanos")
          val sortWithMissing      = sortBy(TestDocument.intField).missing(First)
          val sortWithMode         = sortBy(TestSubDocument.intFieldList).mode(Avg)
          val sortWithNumericType  = sortBy(TestDocument.intField).numericType(NumTypeLong)
          val sortWithOrder        = sortBy(TestDocument.intField).order(Desc)
          val sortWithUnmappedType = sortBy(TestDocument.intField).unmappedType("long")
          val sortWithAllParams = sortBy(TestDocument.dateField)
            .format("strict_date_optional_time_nanos")
            .missing(First)
            .mode(Avg)
            .numericType(NumTypeLong)
            .order(Desc)
            .unmappedType("long")

          val expected =
            """
              |"intField"
              |""".stripMargin

          val expectedWithFormat =
            """
              |{
              |  "dateField": {
              |    "format": "strict_date_optional_time_nanos"
              |  }
              |}
              |""".stripMargin

          val expectedWithMissing =
            """
              |{
              |  "intField": {
              |    "missing": "_first"
              |  }
              |}
              |""".stripMargin

          val expectedWithMode =
            """
              |{
              |  "intFieldList": {
              |    "mode": "avg"
              |  }
              |}
              |""".stripMargin

          val expectedWithNumericType =
            """
              |{
              |  "intField": {
              |    "numeric_type": "long"
              |  }
              |}
              |""".stripMargin

          val expectedWithOrder =
            """
              |{
              |  "intField": {
              |    "order": "desc"
              |  }
              |}
              |""".stripMargin

          val expectedWithUnmappedType =
            """
              |{
              |  "intField": {
              |    "unmapped_type": "long"
              |  }
              |}
              |""".stripMargin

          val expectedWithAllParams =
            """
              |{
              |  "dateField": {
              |    "format": "strict_date_optional_time_nanos", "missing": "_first", "mode": "avg", "numeric_type": "long", "order": "desc", "unmapped_type": "long"
              |  }
              |}
              |""".stripMargin

          assert(sort.toJson)(equalTo(expected.toJson)) &&
          assert(sortWithFormat.toJson)(equalTo(expectedWithFormat.toJson)) &&
          assert(sortWithMissing.toJson)(equalTo(expectedWithMissing.toJson)) &&
          assert(sortWithMode.toJson)(equalTo(expectedWithMode.toJson)) &&
          assert(sortWithNumericType.toJson)(equalTo(expectedWithNumericType.toJson)) &&
          assert(sortWithOrder.toJson)(equalTo(expectedWithOrder.toJson)) &&
          assert(sortWithUnmappedType.toJson)(equalTo(expectedWithUnmappedType.toJson)) &&
          assert(sortWithAllParams.toJson)(equalTo(expectedWithAllParams.toJson))
        },
        test("sortByScript") {
          val sort = sortBy(script = Script.from("doc['day_of_week'].value"), sourceType = NumberType)
          val sortWithMode = sortBy(
            script = Script.from("doc['day_of_week'].value * params['factor']").params("factor" -> 2),
            sourceType = NumberType
          ).mode(Avg)
          val sortWithOrder =
            sortBy(script = Script.from("doc['day_of_week'].value").lang("painless"), sourceType = NumberType)
              .order(Desc)
          val sortWithModeAndOrder = sortBy(
            Script.from("doc['day_of_week'].value * params['factor']").params("factor" -> 2).lang("painless"),
            NumberType
          ).mode(Avg).order(Asc)

          val expected =
            """
              |{
              |  "_script": {
              |    "type": "number",
              |    "script": {
              |      "source": "doc['day_of_week'].value"
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithMode =
            """
              |{
              |  "_script": {
              |    "type": "number",
              |    "script": {
              |      "source": "doc['day_of_week'].value * params['factor']",
              |      "params": {
              |        "factor": 2
              |      }
              |    },
              |    "mode": "avg"
              |  }
              |}
              |""".stripMargin

          val expectedWithOrder =
            """
              |{
              |  "_script": {
              |    "type": "number",
              |    "script": {
              |      "lang": "painless",
              |      "source": "doc['day_of_week'].value"
              |    },
              |    "order": "desc"
              |  }
              |}
              |""".stripMargin

          val expectedWithModeAndOrder =
            """
              |{
              |  "_script": {
              |    "type": "number",
              |    "script": {
              |      "lang": "painless",
              |      "source": "doc['day_of_week'].value * params['factor']",
              |      "params": {
              |        "factor": 2
              |      }
              |    },
              |    "mode": "avg",
              |    "order": "asc"
              |  }
              |}
              |""".stripMargin

          assert(sort.toJson)(equalTo(expected.toJson)) &&
          assert(sortWithMode.toJson)(equalTo(expectedWithMode.toJson)) &&
          assert(sortWithOrder.toJson)(equalTo(expectedWithOrder.toJson)) &&
          assert(sortWithModeAndOrder.toJson)(equalTo(expectedWithModeAndOrder.toJson))
        }
      )
    )
  }
}
