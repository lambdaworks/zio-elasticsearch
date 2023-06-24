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
          val source           = "doc['day_of_week'].value"
          val sourceWithParams = "doc['day_of_week'].value * params['factor']"
          val params           = "factor" -> 2
          val lang             = Painless

          val script           = Script(source)
          val scriptWithParams = Script(sourceWithParams).params(params)
          val scriptWithLang   = Script(sourceWithParams).lang(lang)
          val scriptWithLangAndParams =
            Script(sourceWithParams).params(params).lang(lang)

          assert(script)(equalTo(Script(source, Map.empty, None))) && assert(
            scriptWithParams
          )(
            equalTo(
              Script(source = sourceWithParams, params = Map(params), lang = None)
            )
          ) && assert(scriptWithLang)(
            equalTo(
              Script(source = sourceWithParams, params = Map.empty, lang = Some(lang))
            )
          ) && assert(scriptWithLangAndParams)(
            equalTo(
              Script(
                source = sourceWithParams,
                params = Map(params),
                lang = Some(lang)
              )
            )
          )
        }
      ),
      suite("encoding as JSON")(
        test("script") {
          val source           = "doc['day_of_week'].value"
          val sourceWithParams = "doc['day_of_week'].value * params['factor']"
          val params           = "factor" -> 2
          val lang             = Painless

          val script           = Script(source)
          val scriptWithParams = Script(sourceWithParams).params(params)
          val scriptWithLang   = Script(source).lang(lang)
          val scriptWithLangAndParams =
            Script(sourceWithParams).params(params).lang(lang)

          val expected =
            """
              |{
              |  "source": "doc['day_of_week'].value"
              |}
              |""".stripMargin

          val expectedWithParams =
            """
              |{
              |  "source": "doc['day_of_week'].value * params['factor']",
              |  "params": {
              |    "factor": 2
              |  }
              |}
              |""".stripMargin

          val expectedWithLang =
            """
              |{
              |  "lang": "painless",
              |  "source": "doc['day_of_week'].value"
              |}
              |""".stripMargin

          val expectedWithLangAndParams =
            """
              |{
              |  "lang": "painless",
              |  "source": "doc['day_of_week'].value * params['factor']",
              |  "params": {
              |    "factor": 2
              |  }
              |}
              |""".stripMargin

          assert(script.toJson)(equalTo(expected.toJson)) && assert(scriptWithParams.toJson)(
            equalTo(expectedWithParams.toJson)
          ) && assert(scriptWithLang.toJson)(equalTo(expectedWithLang.toJson)) && assert(
            scriptWithLangAndParams.toJson
          )(equalTo(expectedWithLangAndParams.toJson))
        }
      )
    )
}
