package zio.elasticsearch

import zio.elasticsearch.ElasticHighlight.highlight
import zio.elasticsearch.domain.{TestNestedField, TestSubDocument}
import zio.elasticsearch.highlights.{HighlightField, Highlights}
import zio.elasticsearch.utils.RichString
import zio.json.ast.Json.{Arr, Bool, Num, Str}
import zio.test.Assertion.equalTo
import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assert}
import zio.{Chunk, Scope}

object HighlightsSpec extends ZIOSpecDefault {

  def spec: Spec[Environment with TestEnvironment with Scope, Any] =
    suite("Highlight")(
      suite("creating Highlight")(
        test("successfully create Highlight with only field given without config") {
          assert(highlight("day_of_week"))(
            equalTo(
              Highlights(
                fields = Chunk(HighlightField("day_of_week"))
              )
            )
          )
        },
        test("successfully create Highlight with two fields given without config") {
          assert(highlight("day_of_week").withHighlight("first_name"))(
            equalTo(
              Highlights(
                fields = Chunk(HighlightField("first_name"), HighlightField("day_of_week"))
              )
            )
          )
        },
        test("successfully create Highlight with only one field given with global config") {
          assert(highlight("day_of_week").withGlobalConfig("type", Str("plain")))(
            equalTo(
              Highlights(
                fields = Chunk(HighlightField("day_of_week")),
                config = Map("type" -> Str("plain"))
              )
            )
          )
        },
        test("successfully create Highlight with only one field given with field config") {
          assert(highlight("day_of_week", Map("type" -> Str("plain"))))(
            equalTo(
              Highlights(
                fields = Chunk(HighlightField("day_of_week", Map("type" -> Str("plain"))))
              )
            )
          )
        },
        test("successfully create Highlight with only field given with both global and field config") {
          assert(
            highlight("day_of_week", Map("type" -> Str("plain"))).withGlobalConfig("pre_tags", Arr(Str("<tag1>")))
          )(
            equalTo(
              Highlights(
                fields = Chunk(HighlightField("day_of_week", Map("type" -> Str("plain")))),
                config = Map("pre_tags" -> Arr(Str("<tag1>")))
              )
            )
          )
        }
      ),
      suite("creating Highlight using accessors from Schema")(
        test("successfully create Highlight with only field given without config") {
          assert(highlight(TestSubDocument.stringField))(
            equalTo(
              Highlights(
                fields = Chunk(HighlightField("stringField"))
              )
            )
          )
        },
        test("successfully create Highlight with two fields given without config") {
          assert(highlight(TestSubDocument.stringField).withHighlight(TestSubDocument.intField))(
            equalTo(
              Highlights(
                fields = Chunk(HighlightField("intField"), HighlightField("stringField"))
              )
            )
          )
        },
        test("successfully create Highlight with nested field ") {
          assert(
            highlight(TestSubDocument.nestedField / TestNestedField.stringField).withGlobalConfig("type", Str("plain"))
          )(
            equalTo(
              Highlights(
                fields = Chunk(HighlightField("nestedField.stringField")),
                config = Map("type" -> Str("plain"))
              )
            )
          )
        },
        test("successfully create Highlight with one field given with global config") {
          assert(highlight(TestSubDocument.stringField).withGlobalConfig("type", Str("plain")))(
            equalTo(
              Highlights(
                fields = Chunk(HighlightField("stringField")),
                config = Map("type" -> Str("plain"))
              )
            )
          )
        }
      ),
      suite("encoding Highlight as JSON")(
        test("properly encode Highlight with only field given without config") {
          val highlightObject = highlight("day_of_week")
          val expected =
            """
              |{
              |  "highlight" : {
              |    "fields" : {
              |      "day_of_week" : {}
              |    }
              |  }
              |}
              |""".stripMargin

          assert(highlightObject.toJson)(equalTo(expected.toJson))
        },
        test("properly encode Highlight with two fields given without config") {
          val highlightObject = highlight("day_of_week").withHighlight("first_name")
          val expected =
            """
              |{
              |  "highlight" : {
              |    "fields" : {
              |      "first_name" : {},
              |      "day_of_week" : {}
              |    }
              |  }
              |}
              |""".stripMargin

          assert(highlightObject.toJson)(equalTo(expected.toJson))
        },
        test("properly encode Highlight with two fields given with global config") {
          val highlightObject =
            highlight("day_of_week").withHighlight("first_name").withGlobalConfig("type", Str("plain"))
          val expected =
            """
              |{
              |  "highlight" : {
              |    "type" : "plain",
              |    "fields" : {
              |      "first_name" : {},
              |      "day_of_week" : {}
              |    }
              |  }
              |}
              |""".stripMargin

          assert(highlightObject.toJson)(equalTo(expected.toJson))
        },
        test("properly encode Highlight with one field given with both global and field config") {
          val highlightObject =
            highlight("day_of_week", config = Map("require_field_match" -> Bool(false)))
              .withGlobalConfig("type", Str("plain"))
          val expected =
            """
              |{
              |  "highlight" : {
              |    "type" : "plain",
              |    "fields" : {
              |      "day_of_week" : {
              |        "require_field_match" : false
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(highlightObject.toJson)(equalTo(expected.toJson))
        },
        test("properly encode Highlight with two fields given with both having global and field config") {
          val highlightObject =
            highlight("day_of_week", Map("require_field_match" -> Bool(false)))
              .withGlobalConfig("type", Str("plain"))
              .withHighlight(
                "first_name",
                Map("matched_fields" -> Arr(Str("comment"), Str("comment.plain")), "type" -> Str("fvh"))
              )
          val expected =
            """
              |{
              |  "highlight" : {
              |    "type" : "plain",
              |    "fields" : {
              |      "day_of_week" : {
              |        "require_field_match" : false
              |      },
              |      "first_name" : {
              |        "matched_fields": [ "comment", "comment.plain" ],
              |        "type": "fvh"
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(highlightObject.toJson)(equalTo(expected.toJson))
        },
        test(
          "properly encode Highlight with two fields given with both having global and field config and explicit order"
        ) {
          val highlightObject =
            highlight("day_of_week", Map("require_field_match" -> Bool(false)))
              .withGlobalConfig("type", Str("plain"))
              .withHighlight(
                "first_name",
                Map("matched_fields" -> Arr(Str("comment"), Str("comment.plain")), "type" -> Str("fvh"))
              )
              .withExplicitFieldOrder
          val expected =
            """
              |{
              |  "highlight" : {
              |    "type" : "plain",
              |    "fields" : [
              |      { "day_of_week" : {
              |          "require_field_match" : false
              |        }
              |      },
              |      { "first_name" : {
              |          "matched_fields": [ "comment", "comment.plain" ],
              |          "type": "fvh"
              |        }
              |      }
              |    ]
              |  }
              |}
              |""".stripMargin

          assert(highlightObject.toJson)(equalTo(expected.toJson))
        },
        test("properly encode Highlight with three fields and multiple configurations") {
          val highlightObject =
            highlight("day_of_week", Map("require_field_match" -> Bool(false)))
              .withGlobalConfig("type", Str("plain"))
              .withGlobalConfig("type", Str("fvh"))
              .withGlobalConfig("fragment_size", Num(150))
              .withHighlight(
                field = "first_name",
                config = Map("matched_fields" -> Arr(Str("comment"), Str("comment.plain")), "type" -> Str("fvh"))
              )
              .withHighlight("last_name")
          val expected =
            """
              |{
              |  "highlight" : {
              |    "type" : "fvh",
              |    "fragment_size" : 150,
              |    "fields" : {
              |      "day_of_week" : {
              |        "require_field_match" : false
              |      },
              |      "first_name" : {
              |        "matched_fields": [ "comment", "comment.plain" ],
              |        "type": "fvh"
              |      },
              |      "last_name" : {}
              |    }
              |  }
              |}
              |""".stripMargin

          assert(highlightObject.toJson)(equalTo(expected.toJson))
        }
      )
    )
}
