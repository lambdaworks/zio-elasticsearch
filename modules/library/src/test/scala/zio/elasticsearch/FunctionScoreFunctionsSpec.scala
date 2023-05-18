package zio.elasticsearch

import zio.Scope
import zio.elasticsearch.ElasticQuery.matches
import zio.elasticsearch.query.DecayFunctionType._
import zio.elasticsearch.query.FieldValueFactorFunctionModifier.LOG
import zio.elasticsearch.query.FunctionScoreFunction._
import zio.elasticsearch.query.MultiValueMode.Max
import zio.elasticsearch.query._
import zio.elasticsearch.script.Script
import zio.elasticsearch.utils.RichString
import zio.test.Assertion._
import zio.test._

object FunctionScoreFunctionsSpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("FunctionScoreFunctions")(
      suite("constructing")(
        test("ExpDecayFunction") {
          val function =
            expDecayFunction("field", origin = "11, 12", scale = "2km")
              .weight(10.0)
              .decay(11.0)
              .multiValueMode(MultiValueMode.Max)
              .offset("1d")
              .withFilter(matches("field", "value"))

          assert(function)(
            equalTo(
              DecayFunction(
                field = "field",
                decayFunctionType = Exp,
                origin = "11, 12",
                scale = "2km",
                offset = Some("1d"),
                decay = Some(11.0),
                weight = Some(10.0),
                multiValueMode = Some(Max),
                filter = Some(Match("field", "value"))
              )
            )
          )
        },
        test("FieldValueFactor") {
          val function =
            fieldValueFactor("fieldName")
              .factor(10.0)
              .withFilter(matches("field", "value"))
              .modifier(LOG)
              .missing(13)

          assert(function)(
            equalTo(
              FieldValueFactor(
                fieldName = "fieldName",
                factor = Some(10.0),
                filter = Some(Match("field", "value")),
                modifier = Some(LOG),
                missing = Some(13),
                weight = None
              )
            )
          )
        },
        test("GaussDecayFunction") {
          val function = gaussDecayFunction("field", origin = "11, 12", scale = "2km")
            .weight(10.0)
            .decay(11.0)
            .multiValueMode(Max)
            .offset("1d")
            .withFilter(matches("field", "value"))

          assert(function)(
            equalTo(
              DecayFunction(
                field = "field",
                decayFunctionType = Gauss,
                origin = "11, 12",
                scale = "2km",
                offset = Some("1d"),
                decay = Some(11.0),
                weight = Some(10.0),
                multiValueMode = Some(Max),
                filter = Some(Match("field", "value"))
              )
            )
          )
        },
        test("LinearDecayFunction") {
          val function =
            linearDecayFunction("field", origin = "11, 12", scale = "2km")
              .weight(10.0)
              .decay(11.0)
              .multiValueMode(Max)
              .offset("1d")
              .withFilter(matches("field", "value"))

          assert(function)(
            equalTo(
              DecayFunction(
                field = "field",
                decayFunctionType = Linear,
                origin = "11, 12",
                scale = "2km",
                offset = Some("1d"),
                decay = Some(11.0),
                weight = Some(10.0),
                multiValueMode = Some(Max),
                filter = Some(Match("field", "value"))
              )
            )
          )
        },
        test("RandomScoreFunction") {
          val function =
            randomScoreFunction().weight(12.0).withFilter(matches("field", "value"))
          val functionWithSeed =
            randomScoreFunction(123456).weight(13.0).withFilter(matches("field", "value"))
          val functionWithSeedAndField =
            randomScoreFunction(12345, "field").weight(14.0).withFilter(matches("field", "value"))

          assert(function)(
            equalTo(
              RandomScoreFunction(
                seedAndField = None,
                weight = Some(12.0),
                filter = Some(Match("field", "value"))
              )
            )
          ) &&
          assert(functionWithSeed)(
            equalTo(
              RandomScoreFunction(
                seedAndField = Some(SeedAndField(seed = 123456, fieldName = "_seq_no")),
                weight = Some(13.0),
                filter = Some(Match("field", "value"))
              )
            )
          ) &&
          assert(functionWithSeedAndField)(
            equalTo(
              RandomScoreFunction(
                seedAndField = Some(SeedAndField(seed = 12345, fieldName = "field")),
                weight = Some(14.0),
                filter = Some(Match("field", "value"))
              )
            )
          )
        },
        test("ScriptScoreFunction") {
          val function = scriptScoreFunction(Script("params.agg1 + params.agg2 > 10"))
            .weight(2.0)
            .withFilter(matches("field", "value"))

          assert(function)(
            equalTo(
              ScriptScoreFunction(
                script = Script(source = "params.agg1 + params.agg2 > 10", Map.empty, None),
                weight = Some(2.0),
                filter = Some(Match("field", "value"))
              )
            )
          )

        },
        test("WeightFunction") {
          val function = weightFunction(10.0).withFilter(matches("field", "value"))

          assert(function)(equalTo(WeightFunction(weight = 10.0, filter = Some(Match("field", "value")))))
        }
      ),
      suite("encoding as Json")(
        test("ExpDecayFunction") {
          val function =
            expDecayFunction("field", origin = "2013-09-17", scale = "10d")
              .weight(10.0)
              .decay(0.5)
              .multiValueMode(Max)
              .offset("5d")
              .withFilter(matches("field", "value"))

          val expected =
            """
              |{
              |  "exp": {
              |    "field": {
              |      "origin": "2013-09-17",
              |      "scale": "10d",
              |      "offset": "5d",
              |      "decay": 0.5
              |    },
              |  "multi_value_mode": "max"
              |  },
              |  "weight": 10.0,
              |  "filter": { "match": { "field": "value" } }
              |}
              |""".stripMargin

          assert(function.toJson)(equalTo(expected.toJson))
        },
        test("FieldValueFactor") {
          val function =
            fieldValueFactor("fieldName")
              .factor(1.2)
              .withFilter(matches("field", "value"))
              .modifier(LOG)
              .missing(13)
              .weight(10.0)

          val expected =
            """
              |{
              |  "field_value_factor": {
              |    "field": "fieldName",
              |    "factor": 1.2,
              |    "modifier": "log",
              |    "missing": 13.0
              |  },
              |  "weight": 10.0,
              |  "filter": { "match": { "field": "value" } }
              |}
              |""".stripMargin

          assert(function.toJson)(equalTo(expected.toJson))
        },
        test("GaussDecayFunction") {
          val function =
            gaussDecayFunction("field", origin = "2013-09-17", scale = "10d")
              .weight(10.0)
              .decay(0.5)
              .multiValueMode(Max)
              .offset("5d")
              .withFilter(matches("field", "value"))

          val expected =
            """
              |{
              |  "gauss": {
              |    "field": {
              |      "origin": "2013-09-17",
              |      "scale": "10d",
              |      "offset": "5d",
              |      "decay": 0.5
              |    },
              |  "multi_value_mode": "max"
              |  },
              |  "weight": 10.0,
              |  "filter": { "match": { "field": "value" } }
              |}
              |""".stripMargin

          assert(function.toJson)(equalTo(expected.toJson))
        },
        test("LinearDecayFunction") {
          val function =
            linearDecayFunction("field", origin = "2013-09-17", scale = "10d")
              .weight(10.0)
              .decay(0.5)
              .multiValueMode(Max)
              .offset("5d")
              .withFilter(matches("field", "value"))

          val expected =
            """
              |{
              |  "linear": {
              |    "field": {
              |      "origin": "2013-09-17",
              |      "scale": "10d",
              |      "offset": "5d",
              |      "decay": 0.5
              |    },
              |  "multi_value_mode": "max"
              |  },
              |  "weight": 10.0,
              |  "filter": { "match": { "field": "value" } }
              |}
              |""".stripMargin

          assert(function.toJson)(equalTo(expected.toJson))
        },
        test("RandomScoreFunction") {
          val function =
            randomScoreFunction()
          val functionWithSeed =
            randomScoreFunction(123456).weight(13.0).withFilter(matches("field", "value"))
          val functionWithSeedAndField =
            randomScoreFunction(12345, "field").weight(14.0).withFilter(matches("field", "value"))

          val expected =
            """
              |{
              |  "random_score": {}
              |}
              |""".stripMargin

          val expectedWithSeed =
            """
              |{
              |  "random_score": {
              |    "seed": 123456,
              |    "field" : "_seq_no"
              |  },
              |  "weight": 13.0,
              |  "filter": { "match": { "field": "value" } }
              |}
              |""".stripMargin

          val expectedWithSeedAndField =
            """
              |{
              |  "random_score": {
              |    "seed": 12345,
              |    "field" : "field"
              |  },
              |  "weight": 14.0,
              |  "filter": { "match": { "field": "value" } }
              |}
              |""".stripMargin

          assert(function.toJson)(equalTo(expected.toJson)) &&
          assert(functionWithSeed.toJson)(equalTo(expectedWithSeed.toJson)) &&
          assert(functionWithSeedAndField.toJson)(equalTo(expectedWithSeedAndField.toJson))
        },
        test("ScriptScoreFunction") {
          val function = scriptScoreFunction(Script("params.agg1 + params.agg2 > 10"))
            .weight(2.0)
            .withFilter(matches("field", "value"))

          val expected =
            """
              |{
              |  "script_score": {
              |    "script": {
              |      "source": "params.agg1 + params.agg2 > 10"
              |    }
              |  },
              |  "weight": 2.0,
              |  "filter": { "match": { "field": "value" } }
              |}
              |""".stripMargin

          assert(function.toJson)(equalTo(expected.toJson))

        },
        test("WeightFunction") {
          val function = weightFunction(10.0).withFilter(matches("field", "value"))

          val expected =
            """
              |{
              |  "weight": 10.0,
              |  "filter": { "match": { "field": "value" } }
              |}
              |""".stripMargin

          assert(function.toJson)(equalTo(expected.toJson))
        }
      )
    )
}