package zio.elasticsearch

import zio.Scope
import zio.elasticsearch.script.{Painless, Script}
import zio.elasticsearch.utils.RichString
import zio.test.Assertion.equalTo
import zio.test._

object ScriptSpec extends ZIOSpecDefault {
  def spec: Spec[Environment with TestEnvironment with Scope, Any] =
    suite("Script")(
      suite("constructing")(
        test("create script with source") {
          val source = "doc['day_of_week'].value"

          assert(Script(source))(equalTo(Script(source, Map.empty, None)))
        },
        test("create script with source and params") {
          val source = "doc['day_of_week'].value * params['factor']"
          val params = "factor" -> 2

          assert(Script(source).params(params))(
            equalTo(
              Script(source = source, params = Map(params), lang = None)
            )
          )
        },
        test("create script with source and lang") {
          val source = "doc['day_of_week'].value * params['factor']"
          val lang   = Painless

          assert(Script(source).lang(lang))(
            equalTo(
              Script(source = source, params = Map.empty, lang = Some(lang))
            )
          )
        },
        test("create script with source, params and lang") {
          val source = "doc['day_of_week'].value * params['factor']"
          val params = "factor" -> 2
          val lang   = Painless

          assert(Script(source).params(params).lang(lang))(
            equalTo(
              Script(
                source = source,
                params = Map(params),
                lang = Some(lang)
              )
            )
          )
        }
      ),
      suite("encoding as JSON")(
        test("create script with source") {
          val script = Script("doc['day_of_week'].value")
          val expected =
            """
              |{
              |  "source": "doc['day_of_week'].value"
              |}
              |""".stripMargin

          assert(script.toJson)(equalTo(expected.toJson))
        },
        test("create script with source and params") {
          val script = Script("doc['day_of_week'].value * params['factor']").params("factor" -> 2)
          val expected =
            """
              |{
              |  "source": "doc['day_of_week'].value * params['factor']",
              |  "params": {
              |    "factor": 2
              |  }
              |}
              |""".stripMargin

          assert(script.toJson)(equalTo(expected.toJson))
        },
        test("create script with source and lang") {
          val script = Script("doc['day_of_week'].value").lang(Painless)
          val expected =
            """
              |{
              |  "lang": "painless",
              |  "source": "doc['day_of_week'].value"
              |}
              |""".stripMargin

          assert(script.toJson)(equalTo(expected.toJson))
        },
        test("create script with source, params and lang") {
          val script =
            Script("doc['day_of_week'].value * params['factor']").params("factor" -> 2).lang(Painless)
          val expected =
            """
              |{
              |  "lang": "painless",
              |  "source": "doc['day_of_week'].value * params['factor']",
              |  "params": {
              |    "factor": 2
              |  }
              |}
              |""".stripMargin

          assert(script.toJson)(equalTo(expected.toJson))
        }
      )
    )
}
