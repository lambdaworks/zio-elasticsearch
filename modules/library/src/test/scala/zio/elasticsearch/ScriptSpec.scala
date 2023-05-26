package zio.elasticsearch

import zio.Scope
import zio.elasticsearch.script.{Painless, Script}
import zio.elasticsearch.utils.RichString
import zio.test.Assertion.equalTo
import zio.test._

object ScriptSpec extends ZIOSpecDefault {
  def spec: Spec[Environment with TestEnvironment with Scope, Any] =
    suite("Script")(
      suite("Creating script")(
        test("successfully create Script with only source") {
          assert(Script("doc['day_of_week'].value"))(equalTo(Script("doc['day_of_week'].value", Map.empty, None)))
        },
        test("successfully create Script with source and params") {
          assert(Script("doc['day_of_week'].value * params['factor']").params("factor" -> 2))(
            equalTo(
              Script(source = "doc['day_of_week'].value * params['factor']", params = Map("factor" -> 2), lang = None)
            )
          )
        },
        test("successfully create Script with source and lang") {
          assert(Script("doc['day_of_week'].value").lang(Painless))(
            equalTo(
              Script(source = "doc['day_of_week'].value", params = Map.empty, lang = Some(Painless))
            )
          )
        },
        test("successfully create Script with source, params and lang") {
          assert(Script("doc['day_of_week'].value * params['factor']").params("factor" -> 2).lang(Painless))(
            equalTo(
              Script(
                source = "doc['day_of_week'].value * params['factor']",
                params = Map("factor" -> 2),
                lang = Some(Painless)
              )
            )
          )
        }
      ),
      suite("encoding Script as JSON")(
        test("properly encode Script with only source") {
          val script = Script("doc['day_of_week'].value")
          val expected =
            """
              |{
              |  "source": "doc['day_of_week'].value"
              |}
              |""".stripMargin

          assert(script.toJson)(equalTo(expected.toJson))
        }
      ),
      suite("encoding Script as JSON")(
        test("properly encode Script with source and params") {
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
        test("properly encode Script with source and lang") {
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
        test("properly encode Script with source, params and lang") {
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
