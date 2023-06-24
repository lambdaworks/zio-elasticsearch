package zio.elasticsearch

import zio.Chunk
import zio.elasticsearch.ElasticHighlight.highlight
import zio.elasticsearch.domain.{TestNestedField, TestSubDocument}
import zio.elasticsearch.highlights.{HighlightField, Highlights}
import zio.elasticsearch.utils.RichString
import zio.json.ast.Json.{Arr, Bool, Num, Str}
import zio.test.Assertion.equalTo
import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assert}

object HighlightsSpec extends ZIOSpecDefault {
  def spec: Spec[TestEnvironment, Any] =
    suite("Highlight")(
      suite("constructing")(
        test("highlight") {
          val highlightObject =
            highlight(field = "day_of_week")
          val highlightWithGlobalConfig =
            highlight(field = "day_of_week").withGlobalConfig(field = "type", Str("plain"))
          val highlightWithHighlight =
            highlight(field = "day_of_week").withHighlight(field = "first_name")
          val highlightWithConfig =
            highlight(field = "day_of_week", config = Map("type" -> Str("plain")))
          val highlightWithConfigAndGlobalConfig =
            highlight(field = "day_of_week", config = Map("type" -> Str("plain")))
              .withGlobalConfig(field = "pre_tags", Arr(Str("<tag1>")))
          val highlightOfStringField =
            highlight(field = TestSubDocument.stringField)
          val highlightOfStringFieldWithHighlight =
            highlight(field = TestSubDocument.stringField).withHighlight(TestSubDocument.intField)
          val highlightOfStringFieldWithGlobalConfig =
            highlight(field = TestSubDocument.stringField).withGlobalConfig("type", value = Str("plain"))
          val highlightOfNestedFieldWithGlobalConfig =
            highlight(field = TestSubDocument.nestedField / TestNestedField.stringField)
              .withGlobalConfig("type", value = Str("plain"))

          assert(highlightObject)(
            equalTo(
              Highlights(
                fields = Chunk(HighlightField("day_of_week"))
              )
            )
          ) && assert(highlightWithHighlight)(
            equalTo(
              Highlights(
                fields = Chunk(HighlightField("first_name"), HighlightField("day_of_week"))
              )
            )
          ) && assert(highlightWithGlobalConfig)(
            equalTo(
              Highlights(
                fields = Chunk(HighlightField("day_of_week")),
                config = Map("type" -> Str("plain"))
              )
            )
          ) && assert(highlightWithConfig)(
            equalTo(
              Highlights(
                fields = Chunk(HighlightField("day_of_week", Map("type" -> Str("plain"))))
              )
            )
          ) && assert(
            highlightWithConfigAndGlobalConfig
          )(
            equalTo(
              Highlights(
                fields = Chunk(HighlightField("day_of_week", Map("type" -> Str("plain")))),
                config = Map("pre_tags" -> Arr(Str("<tag1>")))
              )
            )
          ) && assert(highlightOfStringField)(
            equalTo(
              Highlights(
                fields = Chunk(HighlightField("stringField"))
              )
            )
          ) && assert(highlightOfStringFieldWithHighlight)(
            equalTo(
              Highlights(
                fields = Chunk(HighlightField("intField"), HighlightField("stringField"))
              )
            )
          ) && assert(
            highlightOfNestedFieldWithGlobalConfig
          )(
            equalTo(
              Highlights(
                fields = Chunk(HighlightField("nestedField.stringField")),
                config = Map("type" -> Str("plain"))
              )
            )
          ) && assert(highlightOfStringFieldWithGlobalConfig)(
            equalTo(
              Highlights(
                fields = Chunk(HighlightField("stringField")),
                config = Map("type" -> Str("plain"))
              )
            )
          )

        }
      ),
      suite("encoding  as JSON")(
        test("highlight") {
          val highlightObject =
            highlight(field = "day_of_week")
          val highlightWithHighlight =
            highlight(field = "day_of_week").withHighlight(field = "first_name")
          val highlightWithHighlightAndGlobalConfig =
            highlight(field = "day_of_week")
              .withHighlight(field = "first_name")
              .withGlobalConfig(field = "type", Str("plain"))
          val highlightWithConfig =
            highlight("day_of_week", config = Map("require_field_match" -> Bool(false)))
              .withGlobalConfig("type", Str("plain"))
          val highlightWithConfigAndHighlight =
            highlight("day_of_week", config = Map("require_field_match" -> Bool(false)))
              .withGlobalConfig("type", Str("plain"))
              .withHighlight(
                "first_name",
                Map("matched_fields" -> Arr(Str("comment"), Str("comment.plain")), "type" -> Str("fvh"))
              )
          val highlightWithConfigHighlightAndExplicitFieldOrder =
            highlight("day_of_week", config = Map("require_field_match" -> Bool(false)))
              .withGlobalConfig("type", Str("plain"))
              .withHighlight(
                "first_name",
                Map("matched_fields" -> Arr(Str("comment"), Str("comment.plain")), "type" -> Str("fvh"))
              )
              .withExplicitFieldOrder
          val highlightWithMultipleConfig =
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
              |  "fields" : {
              |    "day_of_week" : {}
              |  }
              |}
              |""".stripMargin
          val expectedWithFirstName =
            """
              |{
              |  "fields" : {
              |    "first_name" : {},
              |    "day_of_week" : {}
              |  }
              |}
              |""".stripMargin
          val expectedPlainWithFirstName =
            """
              |{
              |  "type" : "plain",
              |  "fields" : {
              |    "first_name" : {},
              |    "day_of_week" : {}
              |  }
              |}
              |""".stripMargin
          val expectedPlainWithRequiredFieldMatch =
            """
              |{
              |  "type" : "plain",
              |  "fields" : {
              |    "day_of_week" : {
              |      "require_field_match" : false
              |    }
              |  }
              |}
              |""".stripMargin
          val expectedPlainWithMatchedFields =
            """
              |{
              |  "type" : "plain",
              |  "fields" : {
              |    "day_of_week" : {
              |      "require_field_match" : false
              |    },
              |    "first_name" : {
              |      "matched_fields": [ "comment", "comment.plain" ],
              |      "type": "fvh"
              |    }
              |  }
              |}
              |""".stripMargin
          val expectedPlainWithArrayOfFields =
            """
              |{
              |  "type" : "plain",
              |  "fields" : [
              |    { "day_of_week" : {
              |        "require_field_match" : false
              |      }
              |    },
              |    { "first_name" : {
              |        "matched_fields": [ "comment", "comment.plain" ],
              |        "type": "fvh"
              |      }
              |    }
              |  ]
              |}
              |""".stripMargin
          val expectedFvhType =
            """
              |{
              |  "type" : "fvh",
              |  "fragment_size" : 150,
              |  "fields" : {
              |    "day_of_week" : {
              |      "require_field_match" : false
              |    },
              |    "first_name" : {
              |      "matched_fields": [ "comment", "comment.plain" ],
              |      "type": "fvh"
              |    },
              |    "last_name" : {}
              |  }
              |}
              |""".stripMargin

          assert(highlightObject.toJson)(
            equalTo(
              expected.toJson
            )
          ) && assert(highlightWithHighlight.toJson)(
            equalTo(
              expectedWithFirstName.toJson
            )
          ) && assert(highlightWithHighlightAndGlobalConfig.toJson)(
            equalTo(
              expectedPlainWithFirstName.toJson
            )
          ) && assert(highlightWithConfig.toJson)(
            equalTo(
              expectedPlainWithRequiredFieldMatch.toJson
            )
          ) && assert(highlightWithConfigAndHighlight.toJson)(
            equalTo(
              expectedPlainWithMatchedFields.toJson
            )
          ) && assert(highlightWithConfigHighlightAndExplicitFieldOrder.toJson)(
            equalTo(
              expectedPlainWithArrayOfFields.toJson
            )
          ) &&
          assert(highlightWithMultipleConfig.toJson)(
            equalTo(
              expectedFvhType.toJson
            )
          )
        }
      )
    )
}
