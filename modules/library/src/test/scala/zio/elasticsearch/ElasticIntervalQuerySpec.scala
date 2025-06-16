package zio.elasticsearch

import zio.elasticsearch.ElasticQuery.intervals
import zio.elasticsearch.domain.TestDocument
import zio.elasticsearch.query.ElasticIntervalQuery.{
  intervalContains,
  intervalEndsWith,
  intervalMatch,
  intervalRange,
  intervalStartsWith,
  intervalWildcard
}
import zio.elasticsearch.query._
import zio.elasticsearch.utils._
import zio.test.Assertion.equalTo
import zio.test._

object ElasticIntervalQuerySpec extends ZIOSpecDefault {
  def spec: Spec[TestEnvironment, Any] =
    suite("ElasticIntervalQuerySpec")(
      test("intervalMatchQuery") {
        val intervalNoOptions: IntervalMatch[String] = intervalMatch("lambda works")

        val intervalWithOptions: IntervalMatch[String] = intervalMatch("lambda works")
          .orderedOn()
          .maxGaps(2)
          .analyzer("standard")

        val filter = IntervalFilter[String](
          before = Some(intervalMatch("before_term")),
          after = Some(intervalMatch("after_term"))
        )

        val intervalWithFilter: IntervalMatch[String] = intervalMatch("lambda works")
          .orderedOff()
          .maxGaps(2)
          .analyzer("standard")
          .filter(filter)

        val queryWithStringField = intervals("stringField", intervalWithOptions)
        val queryWithTypedField  = intervals(TestDocument.stringField, intervalWithOptions)
        val queryWithFilter      = intervals("stringField", intervalWithFilter)

        val expectedNoOptions =
          """
            |{
            |  "intervals": {
            |    "stringField": {
            |      "match": {
            |        "query": "lambda works"
            |      }
            |    }
            |  }
            |}
            |""".stripMargin

        val expectedWithOptions =
          """
            |{
            |  "intervals": {
            |    "stringField": {
            |      "match": {
            |        "query": "lambda works",
            |        "analyzer": "standard",
            |        "max_gaps": 2,
            |        "ordered": true
            |      }
            |    }
            |  }
            |}
            |""".stripMargin

        val expectedWithFilter =
          """
            |{
            |  "intervals": {
            |    "stringField": {
            |      "match": {
            |        "query": "lambda works",
            |        "analyzer": "standard",
            |        "max_gaps": 2,
            |        "ordered": false,
            |        "filter": {
            |          "before": {
            |            "match": {
            |              "query": "before_term"
            |            }
            |          },
            |          "after": {
            |            "match": {
            |              "query": "after_term"
            |            }
            |          }
            |        }
            |      }
            |    }
            |  }
            |}
            |""".stripMargin

        assert(intervalNoOptions)(
          equalTo(
            IntervalMatch[String](
              query = "lambda works",
              analyzer = None,
              useField = None,
              maxGaps = None,
              ordered = None,
              filter = None
            )
          )
        ) &&
        assert(intervals("stringField", intervalNoOptions).toJson(None))(
          equalTo(expectedNoOptions.toJson)
        ) &&
        assert(queryWithStringField.toJson(None))(
          equalTo(expectedWithOptions.toJson)
        ) &&
        assert(queryWithTypedField.toJson(None))(
          equalTo(expectedWithOptions.toJson)
        ) &&
        assert(queryWithFilter.toJson(None))(
          equalTo(expectedWithFilter.toJson)
        )
      },
      test("intervalRange") {
        val intervalWithBounds = intervalRange[Any, Inclusive, Exclusive](
          lower = Some(Bound("10", InclusiveBound)),
          upper = Some(Bound("20", ExclusiveBound)),
          analyzer = Some("standard"),
          useField = Some("otherField")
        )

        val intervalWithOnlyLower = intervalRange[Any, Inclusive, Inclusive](
          lower = Some(Bound("10", InclusiveBound)),
          upper = None,
          analyzer = Some("standard"),
          useField = Some("otherField")
        )

        val queryWithOnlyLower = intervals("stringField", intervalWithOnlyLower)
        val queryWithBounds    = intervals("stringField", intervalWithBounds)

        val expectedWithBounds =
          """
            |{
            |  "intervals": {
            |    "stringField": {
            |      "range": {
            |        "gte": "10",
            |        "lt": "20",
            |        "analyzer": "standard",
            |        "use_field": "otherField"
            |      }
            |    }
            |  }
            |}
            |""".stripMargin

        val expectedWithOnlyLower =
          """
            |{
            |  "intervals": {
            |    "stringField": {
            |      "range": {
            |        "gte": "10",
            |        "analyzer": "standard",
            |        "use_field": "otherField"
            |      }
            |    }
            |  }
            |}
            |""".stripMargin

        assert(queryWithOnlyLower.toJson(None))(
          equalTo(expectedWithOnlyLower.toJson)
        ) &&
        assert(queryWithBounds.toJson(None))(
          equalTo(expectedWithBounds.toJson)
        )
      },
      test("intervalWildcard") {
        val wildcardExact: IntervalWildcard[String] =
          intervalWildcard("la*mb?da")

        val wildcardContains: IntervalWildcard[String] =
          intervalContains("lambda")

        val wildcardStartsWith: IntervalWildcard[String] =
          intervalStartsWith("lambda")

        val wildcardEndsWith: IntervalWildcard[String] =
          intervalEndsWith("lambda")

        val queryExact: Intervals[String] =
          Intervals("stringField", wildcardExact)

        val queryContains: Intervals[String] =
          Intervals("stringField", wildcardContains)

        val queryStartsWith: Intervals[String] =
          Intervals("stringField", wildcardStartsWith)

        val queryEndsWith: Intervals[String] =
          Intervals("stringField", wildcardEndsWith)

        val expectedExact =
          """
            |{
            |  "intervals": {
            |    "stringField": {
            |      "wildcard": {
            |        "pattern": "la*mb?da"
            |      }
            |    }
            |  }
            |}
            |""".stripMargin

        val expectedContains =
          """
            |{
            |  "intervals": {
            |    "stringField": {
            |      "wildcard": {
            |        "pattern": "*lambda*"
            |      }
            |    }
            |  }
            |}
            |""".stripMargin

        val expectedStartsWith =
          """
            |{
            |  "intervals": {
            |    "stringField": {
            |      "wildcard": {
            |        "pattern": "lambda*"
            |      }
            |    }
            |  }
            |}
            |""".stripMargin

        val expectedEndsWith =
          """
            |{
            |  "intervals": {
            |    "stringField": {
            |      "wildcard": {
            |        "pattern": "*lambda"
            |      }
            |    }
            |  }
            |}
            |""".stripMargin

        assert(wildcardExact)(
          equalTo(
            IntervalWildcard[String](
              pattern = "la*mb?da",
              analyzer = None,
              useField = None
            )
          )
        ) &&
        assert(queryExact.toJson(None))(equalTo(expectedExact.toJson)) &&
        assert(queryContains.toJson(None))(equalTo(expectedContains.toJson)) &&
        assert(queryStartsWith.toJson(None))(equalTo(expectedStartsWith.toJson)) &&
        assert(queryEndsWith.toJson(None))(equalTo(expectedEndsWith.toJson))
      }
    )
}
