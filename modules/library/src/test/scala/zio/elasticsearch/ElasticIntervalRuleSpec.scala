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
  intervalFilter,
  intervalFuzzy,
  intervalMatch,
  intervalPrefix,
  intervalRange,
  intervalRegexp,
  intervalStartsWith,
  intervalWildcard
}
import zio.elasticsearch.ElasticQuery.{intervals, nested}
import zio.elasticsearch.domain.{TestDocument, TestNestedField, TestSubDocument}
import zio.elasticsearch.query._
import zio.elasticsearch.script.Script
import zio.elasticsearch.utils._
import zio.json.ast.Json.{Obj, Str}
import zio.test.Assertion.equalTo
import zio.test._

object ElasticIntervalRuleSpec extends ZIOSpecDefault {
  def spec: Spec[TestEnvironment, Any] = {
    suite("ElasticIntervalRuleSpec")(
      test("intervalMatch") {
        val intervalNoOptions: IntervalMatchRule[Any] = intervalMatch("lambda works")

        val intervalWithOptions: IntervalMatchRule[Any] = intervalMatch("lambda works").orderedOn
          .maxGaps(2)
          .analyzer("standard")

        val filter = IntervalFilter[Any](
          before = Some(intervalMatch("before_term")),
          after = Some(intervalMatch("after_term"))
        )

        val intervalWithFilter = intervalMatch("lambda works").filter(filter)

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
            |        "filter": {
            |          "after": {
            |            "match": {
            |              "query": "after_term"
            |            }
            |          },
            |          "before": {
            |            "match": {
            |              "query": "before_term"
            |            }
            |          }
            |        }
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
        ) &&
        assert(queryWithFilter.toJson(None))(
          equalTo(expectedWithFilter.toJson)
        )
      },
      test("intervalRange") {
        val intervalWithBounds =
          intervalRange.gte("10").lte("20").analyzer("standard").useField("stringField")

        val intervalWithOnlyLower =
          intervalRange.gte("10").analyzer("standard").useField("stringField")

        val intervalWithOnlyUpper =
          intervalRange.lte("20").analyzer("standard").useField("stringField")

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
      test("intervalRange with exclusive bounds") {
        val query = intervals("stringField", intervalRange.gt("10").lt("20"))

        val expected =
          """
            |{
            |  "intervals": {
            |    "stringField": {
            |      "range": {
            |        "gt": "10",
            |        "lt": "20"
            |      }
            |    }
            |  }
            |}
            |""".stripMargin

        assert(query.toJson(None))(equalTo(expected.toJson))
      },
      test("intervalRegexp") {
        val query = intervals("stringField", intervalRegexp("la.*da").analyzer("standard").useField("otherField"))

        val expected =
          """
            |{
            |  "intervals": {
            |    "stringField": {
            |      "regexp": {
            |        "pattern": "la.*da",
            |        "analyzer": "standard",
            |        "use_field": "otherField"
            |      }
            |    }
            |  }
            |}
            |""".stripMargin

        assert(query.toJson(None))(equalTo(expected.toJson))
      },
      test("intervalRule with type-safe useField") {
        val fuzzyRule: IntervalFuzzyRule[TestDocument] =
          intervalFuzzy("lambda").useField(TestDocument.stringField)
        val matchRule: IntervalMatchRule[TestSubDocument] =
          intervalMatch("lambda works").useField(TestSubDocument.nestedField / TestNestedField.stringField)
        val prefixRule: IntervalPrefixRule[TestDocument] =
          intervalPrefix("lamb").useField(TestDocument.stringField)
        val rangeRule: IntervalRangeRule[TestDocument, GreaterThanOrEqualTo[String], Unbounded.type] =
          intervalRange.gte("10").useField(TestDocument.stringField)
        val regexpRule: IntervalRegexpRule[TestDocument] =
          intervalRegexp("la.*da").useField(TestDocument.stringField)
        val wildcardRule: IntervalWildcardRule[TestDocument] =
          intervalWildcard("la*da").useField(TestDocument.stringField)

        val expectedFuzzy =
          """
            |{
            |  "intervals": {
            |    "stringField": {
            |      "fuzzy": {
            |        "term": "lambda",
            |        "use_field": "stringField"
            |      }
            |    }
            |  }
            |}
            |""".stripMargin

        val expectedMatch =
          """
            |{
            |  "intervals": {
            |    "stringField": {
            |      "match": {
            |        "query": "lambda works",
            |        "use_field": "nestedField.stringField"
            |      }
            |    }
            |  }
            |}
            |""".stripMargin

        val expectedPrefix =
          """
            |{
            |  "intervals": {
            |    "stringField": {
            |      "prefix": {
            |        "prefix": "lamb",
            |        "use_field": "stringField"
            |      }
            |    }
            |  }
            |}
            |""".stripMargin

        val expectedRange =
          """
            |{
            |  "intervals": {
            |    "stringField": {
            |      "range": {
            |        "gte": "10",
            |        "use_field": "stringField"
            |      }
            |    }
            |  }
            |}
            |""".stripMargin

        val expectedRegexp =
          """
            |{
            |  "intervals": {
            |    "stringField": {
            |      "regexp": {
            |        "pattern": "la.*da",
            |        "use_field": "stringField"
            |      }
            |    }
            |  }
            |}
            |""".stripMargin

        val expectedWildcard =
          """
            |{
            |  "intervals": {
            |    "stringField": {
            |      "wildcard": {
            |        "pattern": "la*da",
            |        "use_field": "stringField"
            |      }
            |    }
            |  }
            |}
            |""".stripMargin

        assert(intervals(TestDocument.stringField, fuzzyRule).toJson(None))(equalTo(expectedFuzzy.toJson)) &&
        assert(intervals(TestSubDocument.stringField, matchRule).toJson(None))(equalTo(expectedMatch.toJson)) &&
        assert(intervals(TestDocument.stringField, prefixRule).toJson(None))(equalTo(expectedPrefix.toJson)) &&
        assert(intervals(TestDocument.stringField, rangeRule).toJson(None))(equalTo(expectedRange.toJson)) &&
        assert(intervals(TestDocument.stringField, regexpRule).toJson(None))(equalTo(expectedRegexp.toJson)) &&
        assert(intervals(TestDocument.stringField, wildcardRule).toJson(None))(equalTo(expectedWildcard.toJson))
      },
      test("intervalRule type-safe useField accepts only fields of the rule's document type") {
        assertZIO(typeCheck {
          """
            import zio.elasticsearch.ElasticIntervalRule.intervalMatch
            import zio.elasticsearch.domain.TestDocument

            intervalMatch("lambda").useField(TestDocument.stringField).useField(TestDocument.stringField)
          """
        })(Assertion.isRight) &&
        assertZIO(typeCheck {
          """
            import zio.elasticsearch.ElasticIntervalRule.intervalMatch
            import zio.elasticsearch.domain.{TestDocument, TestSubDocument}

            intervalMatch("lambda").useField(TestDocument.stringField).useField(TestSubDocument.stringField)
          """
        })(Assertion.isLeft)
      },
      test("intervals accepts only rules defined for the field's document type") {
        assertZIO(typeCheck {
          """
            import zio.NonEmptyChunk
            import zio.elasticsearch.ElasticIntervalRule.{intervalAllOf, intervalFilter, intervalMatch}
            import zio.elasticsearch.ElasticQuery.intervals
            import zio.elasticsearch.domain.TestDocument

            intervals(
              TestDocument.stringField,
              intervalAllOf(NonEmptyChunk(intervalMatch("a").useField(TestDocument.stringField), intervalMatch("b")))
                .filter(intervalFilter(before = Some(intervalMatch("c").useField(TestDocument.stringField))))
            )
          """
        })(Assertion.isRight) &&
        assertZIO(typeCheck {
          """
            import zio.elasticsearch.ElasticIntervalRule.intervalMatch
            import zio.elasticsearch.ElasticQuery.intervals
            import zio.elasticsearch.domain.{TestDocument, TestSubDocument}

            intervals(TestDocument.stringField, intervalMatch("a").useField(TestSubDocument.stringField))
          """
        })(Assertion.isLeft) &&
        assertZIO(typeCheck {
          """
            import zio.NonEmptyChunk
            import zio.elasticsearch.ElasticIntervalRule.{intervalAnyOf, intervalMatch}
            import zio.elasticsearch.ElasticQuery.intervals
            import zio.elasticsearch.domain.{TestDocument, TestSubDocument}

            intervals(
              TestDocument.stringField,
              intervalAnyOf(NonEmptyChunk(intervalMatch("a").useField(TestSubDocument.stringField), intervalMatch("b")))
            )
          """
        })(Assertion.isLeft) &&
        assertZIO(typeCheck {
          """
            import zio.elasticsearch.ElasticIntervalRule.{intervalFilter, intervalMatch}
            import zio.elasticsearch.ElasticQuery.intervals
            import zio.elasticsearch.domain.{TestDocument, TestSubDocument}

            intervals(
              TestDocument.stringField,
              intervalMatch("a").filter(intervalFilter(before = Some(intervalMatch("b").useField(TestSubDocument.stringField))))
            )
          """
        })(Assertion.isLeft)
      },
      test("intervalRange accepts only one lower and one upper bound") {
        assertZIO(typeCheck {
          """
            import zio.elasticsearch.ElasticIntervalRule.intervalRange

            intervalRange.gt("10").lte("20")
          """
        })(Assertion.isRight) &&
        assertZIO(typeCheck {
          """
            import zio.elasticsearch.ElasticIntervalRule.intervalRange

            intervalRange.gt("10").gte("20")
          """
        })(Assertion.isLeft) &&
        assertZIO(typeCheck {
          """
            import zio.elasticsearch.ElasticIntervalRule.intervalRange

            intervalRange.lt("10").lte("20")
          """
        })(Assertion.isLeft)
      },
      test("intervalFilter with script") {
        val query = intervals(
          "stringField",
          intervalMatch("lambda").filter(
            intervalFilter(script = Some(Script("interval.start > 10 && interval.gaps == 0").params("limit" -> 2)))
          )
        )

        val expected =
          """
            |{
            |  "intervals": {
            |    "stringField": {
            |      "match": {
            |        "query": "lambda",
            |        "filter": {
            |          "script": {
            |            "source": "interval.start > 10 && interval.gaps == 0",
            |            "params": {
            |              "limit": 2
            |            }
            |          }
            |        }
            |      }
            |    }
            |  }
            |}
            |""".stripMargin

        assert(query.toJson(None))(equalTo(expected.toJson))
      },
      test("intervals inside nested query prefixes use_field with the nested path") {
        val query = nested(
          TestDocument.subDocumentList,
          intervals(
            TestSubDocument.stringField,
            intervalMatch("lambda")
              .useField(TestSubDocument.intField)
              .filter(intervalFilter(before = Some(intervalPrefix("wor").useField("stringField"))))
          )
        )

        val expected =
          """
            |{
            |  "nested": {
            |    "path": "subDocumentList",
            |    "query": {
            |      "intervals": {
            |        "subDocumentList.stringField": {
            |          "match": {
            |            "query": "lambda",
            |            "use_field": "subDocumentList.intField",
            |            "filter": {
            |              "before": {
            |                "prefix": {
            |                  "prefix": "wor",
            |                  "use_field": "subDocumentList.stringField"
            |                }
            |              }
            |            }
            |          }
            |        }
            |      }
            |    }
            |  }
            |}
            |""".stripMargin

        assert(query.toJson(None))(equalTo(expected.toJson))
      },
      test("intervalContains, intervalStartsWith and intervalEndsWith match wildcard characters literally") {
        def expected(pattern: String) =
          Obj("intervals" -> Obj("stringField" -> Obj("wildcard" -> Obj("pattern" -> Str(pattern)))))

        assert(intervals("stringField", intervalContains("a*b?c\\d")).toJson(None))(
          equalTo(expected("*a\\*b\\?c\\\\d*"))
        ) &&
        assert(intervals("stringField", intervalStartsWith("a*b")).toJson(None))(equalTo(expected("a\\*b*"))) &&
        assert(intervals("stringField", intervalEndsWith("a?b")).toJson(None))(equalTo(expected("*a\\?b")))
      },
      test("intervalFuzzy") {
        val query = intervals(
          "stringField",
          intervalFuzzy("lambda").fuzziness("AUTO").prefixLength(1).transpositionsEnabled.analyzer("standard")
        )

        val expected =
          """
            |{
            |  "intervals": {
            |    "stringField": {
            |      "fuzzy": {
            |        "term": "lambda",
            |        "prefix_length": 1,
            |        "transpositions": true,
            |        "fuzziness": "AUTO",
            |        "analyzer": "standard"
            |      }
            |    }
            |  }
            |}
            |""".stripMargin

        assert(query.toJson(None))(equalTo(expected.toJson))
      },
      test("intervalWildcard") {
        val wildcardExact: IntervalWildcardRule[Any] =
          intervalWildcard("la*mb?da")

        val wildcardContains: IntervalWildcardRule[Any] =
          intervalContains("lambda")

        val wildcardStartsWith: IntervalWildcardRule[Any] =
          intervalStartsWith("lambda")

        val wildcardEndsWith: IntervalWildcardRule[Any] =
          intervalEndsWith("lambda")

        val queryExact: Intervals[Any] =
          Intervals("stringField", wildcardExact)

        val queryContains: Intervals[Any] =
          Intervals("stringField", wildcardContains)

        val queryStartsWith: Intervals[Any] =
          Intervals("stringField", wildcardStartsWith)

        val queryEndsWith: Intervals[Any] =
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
