package zio.elasticsearch

import zio.Scope
import zio.elasticsearch.script.Script
import zio.elasticsearch.utils.RichString
import zio.test.Assertion.equalTo
import zio.test._

object ScriptSpec extends ZIOSpecDefault {
  def spec: Spec[Environment with TestEnvironment with Scope, Any] =
    suite("Script")(
      suite("Creating script")(
        test("successfully create Script with only source") {
          assert(Script.from("doc['day_of_week'].value"))(equalTo(Script("doc['day_of_week'].value", Map.empty, None)))
        },
        test("successfully create Script with source and params") {
          assert(Script.from("doc['day_of_week'].value * params['factor']").withParams("factor" -> 2))(
            equalTo(Script.from("doc['day_of_week'].value * params['factor']").withParams("factor" -> 2))
          )
        },
        test("successfully create Script with source and lang") {
          assert(Script.from("doc['day_of_week'].value").lang("painless"))(
            equalTo(Script.from("doc['day_of_week'].value").lang("painless"))
          )
        },
        test("successfully create Script with source, params and lang") {
          assert(Script.from("doc['day_of_week'].value * params['factor']").withParams("factor" -> 2).lang("painless"))(
            equalTo(
              Script.from("doc['day_of_week'].value * params['factor']").withParams("factor" -> 2).lang("painless")
            )
              HttpExecutorSpec.scala
          )
        }
      ),
      suite("encoding Script as JSON")(
        test("properly encode Script with only source") {
          val script = Script.from("doc['day_of_week'].value")
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
          val script = Script.from("doc['day_of_week'].value * params['factor']").withParams("factor" -> 2)
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
          val script = Script.from("doc['day_of_week'].value").lang("painless")
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
            Script.from("doc['day_of_week'].value * params['factor']").withParams("factor" -> 2).lang("painless")
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
