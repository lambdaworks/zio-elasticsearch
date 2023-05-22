package zio.elasticsearch

import zio.Chunk
import zio.elasticsearch.ElasticHighlight.highlight
import zio.elasticsearch.highlights.{HighlightField, Highlights}
import zio.elasticsearch.query.InnerHits
import zio.elasticsearch.utils.RichString
import zio.json.ast.Json.Obj
import zio.test.Assertion.equalTo
import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assert}

object InnerHitsSpec extends ZIOSpecDefault {

  def spec: Spec[TestEnvironment, Any] =
    suite("InnerHits")(
      test("constructing") {
        val innerHits               = InnerHits()
        val innerHitsWithExcluded   = InnerHits().excludes("longField")
        val innerHitsWithFrom       = InnerHits().from(2)
        val innerHitsWithHighlights = InnerHits().highlights(highlight("stringField"))
        val innerHitsWithIncluded   = InnerHits().includes("intField")
        val innerHitsWithName       = InnerHits().name("innerHitName")
        val innerHitsWithSize       = InnerHits().size(5)
        val innerHitsWithAllParams =
          InnerHits()
            .excludes("longField")
            .includes("intField")
            .from(2)
            .highlights(highlight("stringField"))
            .name("innerHitName")
            .size(5)

        assert(innerHits)(
          equalTo(
            InnerHits(excluded = Chunk(), from = None, highlights = None, included = Chunk(), name = None, size = None)
          )
        ) && assert(innerHitsWithExcluded)(
          equalTo(
            InnerHits(
              excluded = Chunk("longField"),
              included = Chunk(),
              from = None,
              highlights = None,
              name = None,
              size = None
            )
          )
        ) && assert(innerHitsWithFrom)(
          equalTo(
            InnerHits(
              excluded = Chunk(),
              included = Chunk(),
              from = Some(2),
              highlights = None,
              name = None,
              size = None
            )
          )
        ) && assert(innerHitsWithHighlights)(
          equalTo(
            InnerHits(
              excluded = Chunk(),
              included = Chunk(),
              from = None,
              highlights = Some(Highlights(fields = Chunk(HighlightField("stringField")), config = Map.empty)),
              name = None,
              size = None
            )
          )
        ) && assert(innerHitsWithIncluded)(
          equalTo(
            InnerHits(
              excluded = Chunk(),
              included = Chunk("intField"),
              from = None,
              highlights = None,
              name = None,
              size = None
            )
          )
        ) && assert(innerHitsWithName)(
          equalTo(
            InnerHits(
              excluded = Chunk(),
              included = Chunk(),
              from = None,
              highlights = None,
              name = Some("innerHitName"),
              size = None
            )
          )
        ) && assert(innerHitsWithSize)(
          equalTo(
            InnerHits(
              excluded = Chunk(),
              included = Chunk(),
              from = None,
              highlights = None,
              name = None,
              size = Some(5)
            )
          )
        ) && assert(innerHitsWithAllParams)(
          equalTo(
            InnerHits(
              excluded = Chunk("longField"),
              included = Chunk("intField"),
              from = Some(2),
              highlights = Some(Highlights(fields = Chunk(HighlightField("stringField")), config = Map.empty)),
              name = Some("innerHitName"),
              size = Some(5)
            )
          )
        )
      },
      test("encoding as JSON") {
        val innerHits               = InnerHits()
        val innerHitsWithExcluded   = InnerHits().excludes("longField")
        val innerHitsWithFrom       = InnerHits().from(2)
        val innerHitsWithHighlights = InnerHits().highlights(highlight("stringField"))
        val innerHitsWithIncluded   = InnerHits().includes("intField")
        val innerHitsWithName       = InnerHits().name("innerHitName")
        val innerHitsWithSize       = InnerHits().size(5)
        val innerHitsWithAllParams =
          InnerHits()
            .excludes("longField")
            .includes("intField")
            .from(2)
            .highlights(highlight("stringField"))
            .name("innerHitName")
            .size(5)

        val expected =
          """
            |{
            |  "inner_hits": {
            |    
            |  }
            |}
            |""".stripMargin

        val expectedWithExcluded =
          """
            |{
            |  "inner_hits": {
            |    "_source" : {
            |      "excludes" : [
            |        "longField"
            |      ]
            |    }
            |  }
            |}
            |""".stripMargin

        val expectedWithFrom =
          """
            |{
            |  "inner_hits": {
            |    "from": 2
            |  }
            |}
            |""".stripMargin

        val expectedWithHighlights =
          """
            |{
            |  "inner_hits": {
            |    "highlight" : {
            |      "fields" : {
            |        "stringField" : {}
            |      }
            |    }
            |  }
            |}
            |""".stripMargin

        val expectedWithIncluded =
          """
            |{
            |  "inner_hits": {
            |    "_source" : {
            |      "includes" : [
            |        "intField"
            |      ]
            |    }
            |  }
            |}
            |""".stripMargin

        val expectedWithName =
          """
            |{
            |  "inner_hits": {
            |    "name": "innerHitName"
            |  }
            |}
            |""".stripMargin

        val expectedWithSize =
          """
            |{
            |  "inner_hits": {
            |    "size": 5
            |  }
            |}
            |""".stripMargin

        val expectedWithAllParams =
          """
            |{
            |  "inner_hits": {
            |    "from": 2,
            |    "size": 5,
            |    "name": "innerHitName",
            |    "highlight" : {
            |      "fields" : {
            |        "stringField" : {}
            |      }
            |    },
            |    "_source" : {
            |      "includes" : [
            |        "intField"
            |      ],
            |      "excludes" : [
            |        "longField"
            |      ]
            |    }
            |  }
            |}
            |""".stripMargin

        assert(Obj(innerHits.toStringJsonPair))(equalTo(expected.toJson)) &&
        assert(Obj(innerHitsWithExcluded.toStringJsonPair))(equalTo(expectedWithExcluded.toJson)) &&
        assert(Obj(innerHitsWithFrom.toStringJsonPair))(equalTo(expectedWithFrom.toJson)) &&
        assert(Obj(innerHitsWithHighlights.toStringJsonPair))(equalTo(expectedWithHighlights.toJson)) &&
        assert(Obj(innerHitsWithIncluded.toStringJsonPair))(equalTo(expectedWithIncluded.toJson)) &&
        assert(Obj(innerHitsWithName.toStringJsonPair))(equalTo(expectedWithName.toJson)) &&
        assert(Obj(innerHitsWithSize.toStringJsonPair))(equalTo(expectedWithSize.toJson)) &&
        assert(Obj(innerHitsWithAllParams.toStringJsonPair))(equalTo(expectedWithAllParams.toJson))
      }
    )
}
