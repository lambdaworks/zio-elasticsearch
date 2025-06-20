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

import zio.elasticsearch.ElasticIntervalRule.{
  intervalContains,
  intervalEndsWith,
  intervalMatch,
  intervalRange,
  intervalStartsWith,
  intervalWildcard
}
import zio.elasticsearch.ElasticQuery.intervals
import zio.elasticsearch.domain.TestDocument
import zio.elasticsearch.query._
import zio.elasticsearch.utils._
import zio.test.Assertion.equalTo
import zio.test._

object ElasticIntervalRuleSpec extends ZIOSpecDefault {
  def spec: Spec[TestEnvironment, Any] = {
    suite("ElasticIntervalRuleSpec")(
      test("intervalMatch") {
        val intervalNoOptions: IntervalMatch[String] = intervalMatch("lambda works")

        val intervalWithOptions: IntervalMatch[String] = intervalMatch("lambda works").orderedOn
          .maxGaps(2)
          .analyzer("standard")

        val filter = IntervalFilter[String](
          before = Some(intervalMatch("before_term")),
          after = Some(intervalMatch("after_term"))
        )

        val queryWithStringField = intervals("stringField", intervalWithOptions)
        val queryWithTypedField  = intervals(TestDocument.stringField, intervalWithOptions)

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

        assert(intervals("stringField", intervalNoOptions).toJson(None))(
          equalTo(expectedNoOptions.toJson)
        ) &&
        assert(queryWithStringField.toJson(None))(
          equalTo(expectedWithOptions.toJson)
        ) &&
        assert(queryWithTypedField.toJson(None))(
          equalTo(expectedWithOptions.toJson)
        )
      },
      test("intervalRange") {
        val intervalWithBounds = intervalRange[Any](
          lower = Some(GreaterThanInterval("10")),
          upper = Some(LessThanInterval("20")),
          analyzer = Some("standard"),
          useField = Some("stringField")
        )

        val intervalWithOnlyLower = intervalRange[Any](
          lower = Some(GreaterThanInterval("10")),
          upper = None,
          analyzer = Some("standard"),
          useField = Some("stringField")
        )

        val intervalWithOnlyUpper = intervalRange[Any](
          lower = None,
          upper = Some(LessThanInterval("20")),
          analyzer = Some("standard"),
          useField = Some("stringField")
        )

        val queryWithBounds = intervals(TestDocument.stringField, intervalWithBounds)
        val queryWithLower  = intervals(TestDocument.stringField, intervalWithOnlyLower)
        val queryWithUpper  = intervals(TestDocument.stringField, intervalWithOnlyUpper)

        val expectedWithBounds =
          """
            |{
            |  "intervals": {
            |    "stringField": {
            |      "range": {
            |        "gte": "10",
            |        "lte": "20",
            |        "analyzer": "standard",
            |        "use_field": "stringField"
            |      }
            |    }
            |  }
            |}
            |""".stripMargin

        val expectedWithLower =
          """
            |{
            |  "intervals": {
            |    "stringField": {
            |      "range": {
            |        "gte": "10",
            |        "analyzer": "standard",
            |        "use_field": "stringField"
            |      }
            |    }
            |  }
            |}
            |""".stripMargin

        val expectedWithUpper =
          """
            |{
            |  "intervals": {
            |    "stringField": {
            |      "range": {
            |        "lte": "20",
            |        "analyzer": "standard",
            |        "use_field": "stringField"
            |      }
            |    }
            |  }
            |}
            |""".stripMargin

        assert(queryWithBounds.toJson(None))(
          equalTo(expectedWithBounds.toJson)
        ) &&
        assert(queryWithLower.toJson(None))(
          equalTo(expectedWithLower.toJson)
        ) &&
        assert(queryWithUpper.toJson(None))(
          equalTo(expectedWithUpper.toJson)
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

        assert(queryExact.toJson(None))(equalTo(expectedExact.toJson)) &&
        assert(queryContains.toJson(None))(equalTo(expectedContains.toJson)) &&
        assert(queryStartsWith.toJson(None))(equalTo(expectedStartsWith.toJson)) &&
        assert(queryEndsWith.toJson(None))(equalTo(expectedEndsWith.toJson))
      },
      test("interval query") {
        val query1 = intervals(TestDocument.stringField, intervalMatch("test query"))

        val query2 = intervals(
          TestDocument.stringField,
          intervalMatch("another test")
            .maxGaps(3)
            .orderedOn
        )

        val query3 = intervals(
          TestDocument.stringField,
          intervalMatch("sample text")
            .analyzer("standard")
        )
        val expectedJson1 =
          """
            |{
            |  "intervals": {
            |    "stringField": {
            |      "match": {
            |        "query": "test query"
            |      }
            |    }
            |  }
            |}
            |""".stripMargin

        val expectedJson2 =
          """
            |{
            |  "intervals": {
            |    "stringField": {
            |      "match": {
            |        "query": "another test",
            |        "max_gaps": 3,
            |        "ordered": true
            |      }
            |    }
            |  }
            |}
            |""".stripMargin

        val expectedJson3 =
          """
            |{
            |  "intervals": {
            |    "stringField": {
            |      "match": {
            |        "query": "sample text",
            |        "analyzer": "standard"
            |      }
            |    }
            |  }
            |}
            |""".stripMargin

        assert(query1.toJson(None))(equalTo(expectedJson1.toJson)) &&
        assert(query2.toJson(None))(equalTo(expectedJson2.toJson)) &&
        assert(query3.toJson(None))(equalTo(expectedJson3.toJson))
      }
    )
  }
}
