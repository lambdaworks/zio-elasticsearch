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
        test("script") {
          val source          = "doc['day_of_week'].value"
          val sourceAndParams = "doc['day_of_week'].value * params['factor']"
          val params          = "factor" -> 2
          val lang            = Painless

          assert(Script(source))(equalTo(Script(source, Map.empty, None))) && assert(
            Script(sourceAndParams).params(params)
          )(
            equalTo(
              Script(source = sourceAndParams, params = Map(params), lang = None)
            )
          ) && assert(Script(sourceAndParams).lang(lang))(
            equalTo(
              Script(source = sourceAndParams, params = Map.empty, lang = Some(lang))
            )
          ) && assert(Script(sourceAndParams).params(params).lang(lang))(
            equalTo(
              Script(
                source = sourceAndParams,
                params = Map(params),
                lang = Some(lang)
              )
            )
          )
        }
      ),
      suite("encoding as JSON")(
        test("script") {
          val scriptOnly = Script("doc['day_of_week'].value")
          val scriptOnlyExpectedVal =
            """
              |{
              |  "source": "doc['day_of_week'].value"
              |}
              |""".stripMargin

          val scriptWithParams = Script("doc['day_of_week'].value * params['factor']").params("factor" -> 2)
          val scriptWithParamsExpectedVal =
            """
              |{
              |  "source": "doc['day_of_week'].value * params['factor']",
              |  "params": {
              |    "factor": 2
              |  }
              |}
              |""".stripMargin

          val scriptWithLang = Script("doc['day_of_week'].value").lang(Painless)
          val scriptWithLangExpectedVal =
            """
              |{
              |  "lang": "painless",
              |  "source": "doc['day_of_week'].value"
              |}
              |""".stripMargin

          val scriptWithParamsAndLang =
            Script("doc['day_of_week'].value * params['factor']").params("factor" -> 2).lang(Painless)
          val scriptWithParamsAndLangExpectedVal =
            """
              |{
              |  "lang": "painless",
              |  "source": "doc['day_of_week'].value * params['factor']",
              |  "params": {
              |    "factor": 2
              |  }
              |}
              |""".stripMargin

          assert(scriptOnly.toJson)(equalTo(scriptOnlyExpectedVal.toJson)) && assert(scriptWithParams.toJson)(
            equalTo(scriptWithParamsExpectedVal.toJson)
          ) && assert(scriptWithLang.toJson)(equalTo(scriptWithLangExpectedVal.toJson)) && assert(
            scriptWithParamsAndLang.toJson
          )(equalTo(scriptWithParamsAndLangExpectedVal.toJson))
        }
      )
    )
}
