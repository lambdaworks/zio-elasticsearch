package zio.elasticsearch

import zio.elasticsearch.ElasticQuery.matches
import zio.elasticsearch.domain.TestDocument
import zio.elasticsearch.query.DecayFunctionType._
import zio.elasticsearch.query.FieldValueFactorFunctionModifier.Log
import zio.elasticsearch.query.FunctionScoreFunction._
import zio.elasticsearch.query.MultiValueMode.Max
import zio.elasticsearch.query._
import zio.elasticsearch.script.Script
import zio.elasticsearch.utils.RichString
import zio.test.Assertion._
import zio.test._

object FunctionScoreSpec extends ZIOSpecDefault {

  def spec: Spec[TestEnvironment, Any] =
    suite("FunctionScore")(
      suite("constructing")(
        test("expDecayFunction") {
          val function =
            expDecayFunction("field", origin = "11, 12", scale = "2km")
              .weight(10.0)
              .decay(11.0)
              .multiValueMode(MultiValueMode.Max)
              .offset("1d")
              .filter(matches("field", "value"))

          val typeSafeFunction =
            expDecayFunction(TestDocument.stringField, origin = "11, 12", scale = "2km")
              .weight(10.0)
              .decay(11.0)
              .multiValueMode(MultiValueMode.Max)
              .offset("1d")
              .filter(matches(TestDocument.intField, 1))

          assert(function)(
            equalTo(
              DecayFunction[Any](
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
          ) && assert(typeSafeFunction)(
            equalTo(
              DecayFunction[TestDocument](
                field = "stringField",
                decayFunctionType = Exp,
                origin = "11, 12",
                scale = "2km",
                offset = Some("1d"),
                decay = Some(11.0),
                weight = Some(10.0),
                multiValueMode = Some(Max),
                filter = Some(Match("intField", 1))
              )
            )
          )
        },
        test("fieldValueFactor") {
          val function =
            fieldValueFactor("fieldName")
              .factor(10.0)
              .filter(matches("field", "value"))
              .modifier(Log)
              .missing(13)

          val typeSafeFunction =
            fieldValueFactor(TestDocument.stringField)
              .factor(10.0)
              .filter(matches(TestDocument.doubleField, 2.0))
              .modifier(Log)
              .missing(13)

          assert(function)(
            equalTo(
              FieldValueFactor[Any](
                field = "fieldName",
                factor = Some(10.0),
                filter = Some(Match("field", "value")),
                modifier = Some(Log),
                missing = Some(13),
                weight = None
              )
            )
          ) && assert(typeSafeFunction)(
            equalTo(
              FieldValueFactor[TestDocument](
                field = "stringField",
                factor = Some(10.0),
                filter = Some(Match("doubleField", 2.0)),
                modifier = Some(Log),
                missing = Some(13),
                weight = None
              )
            )
          )
        },
        test("gaussDecayFunction") {
          val function = gaussDecayFunction("field", origin = "11, 12", scale = "2km")
            .weight(10.0)
            .decay(11.0)
            .multiValueMode(Max)
            .offset("1d")
            .filter(matches("field", "value"))

          assert(function)(
            equalTo(
              DecayFunction[Any](
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
        test("linearDecayFunction") {
          val function =
            linearDecayFunction("field", origin = "11, 12", scale = "2km")
              .weight(10.0)
              .decay(11.0)
              .multiValueMode(Max)
              .offset("1d")
              .filter(matches("field", "value"))

          val typeSafeFunction =
            linearDecayFunction(TestDocument.locationField, origin = "11, 12", scale = "2km")
              .weight(10.0)
              .decay(11.0)
              .multiValueMode(Max)
              .offset("1d")
              .filter(matches(TestDocument.stringField, "value"))

          assert(function)(
            equalTo(
              DecayFunction[Any](
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
          ) && assert(typeSafeFunction)(
            equalTo(
              DecayFunction[TestDocument](
                field = "locationField",
                decayFunctionType = Linear,
                origin = "11, 12",
                scale = "2km",
                offset = Some("1d"),
                decay = Some(11.0),
                weight = Some(10.0),
                multiValueMode = Some(Max),
                filter = Some(Match("stringField", "value"))
              )
            )
          )
        },
        test("randomScoreFunction") {
          val function =
            randomScoreFunction().weight(12.0).filter(matches(TestDocument.stringField, "value"))
          val functionWithSeed =
            randomScoreFunction(123456).weight(13.0).filter(matches("field", "value"))
          val functionWithSeedAndField =
            randomScoreFunction(12345, "field").weight(14.0).filter(matches("field", "value"))

          assert(function)(
            equalTo(
              RandomScoreFunction[TestDocument](
                seedAndField = None,
                weight = Some(12.0),
                filter = Some(Match("stringField", "value"))
              )
            )
          ) &&
          assert(functionWithSeed)(
            equalTo(
              RandomScoreFunction[Any](
                seedAndField = Some(SeedAndField(seed = 123456, fieldName = "_seq_no")),
                weight = Some(13.0),
                filter = Some(Match("field", "value"))
              )
            )
          ) &&
          assert(functionWithSeedAndField)(
            equalTo(
              RandomScoreFunction[Any](
                seedAndField = Some(SeedAndField(seed = 12345, fieldName = "field")),
                weight = Some(14.0),
                filter = Some(Match("field", "value"))
              )
            )
          )
        },
        test("scriptScoreFunction") {
          val function =
            scriptScoreFunction(Script("params.agg1 + params.agg2 > 10"))
              .weight(2.0)
              .filter(matches("field", "value"))

          val typeSafeFunction =
            scriptScoreFunction(Script("params.agg1 + params.agg2 > 10"))
              .weight(2.0)
              .filter(matches(TestDocument.stringField, "value"))

          assert(function)(
            equalTo(
              ScriptScoreFunction[Any](
                script = Script(source = "params.agg1 + params.agg2 > 10", Map.empty, None),
                weight = Some(2.0),
                filter = Some(Match("field", "value"))
              )
            )
          ) && assert(typeSafeFunction)(
            equalTo(
              ScriptScoreFunction[TestDocument](
                script = Script(source = "params.agg1 + params.agg2 > 10", Map.empty, None),
                weight = Some(2.0),
                filter = Some(Match("stringField", "value"))
              )
            )
          )

        },
        test("weightFunction") {
          val function = weightFunction(10.0).filter(matches("field", "value"))

          val typeSafeFunction = weightFunction(10.0).filter(matches(TestDocument.stringField, "value"))

          assert(function)(equalTo(WeightFunction[Any](weight = 10.0, filter = Some(Match("field", "value"))))) &&
          assert(typeSafeFunction)(
            equalTo(WeightFunction[TestDocument](weight = 10.0, filter = Some(Match("stringField", "value"))))
          )
        }
      ),
      suite("encoding as JSON")(
        test("expDecayFunction") {
          val function =
            expDecayFunction("field", origin = "2013-09-17", scale = "10d")
              .weight(10.0)
              .decay(0.5)
              .multiValueMode(Max)
              .offset("5d")
              .filter(matches("field", "value"))

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
        test("fieldValueFactor") {
          val function =
            fieldValueFactor("fieldName")
              .factor(1.2)
              .filter(matches("field", "value"))
              .modifier(Log)
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
        test("gaussDecayFunction") {
          val function =
            gaussDecayFunction("field", origin = "2013-09-17", scale = "10d")
              .weight(10.0)
              .decay(0.5)
              .multiValueMode(Max)
              .offset("5d")
              .filter(matches("field", "value"))

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
        test("linearDecayFunction") {
          val function =
            linearDecayFunction("field", origin = "2013-09-17", scale = "10d")
              .weight(10.0)
              .decay(0.5)
              .multiValueMode(Max)
              .offset("5d")
              .filter(matches("field", "value"))

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
        test("randomScoreFunction") {
          val function =
            randomScoreFunction()
          val functionWithSeed =
            randomScoreFunction(123456).weight(13.0).filter(matches("field", "value"))
          val functionWithSeedAndField =
            randomScoreFunction(12345, "field").weight(14.0).filter(matches("field", "value"))

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
        test("scriptScoreFunction") {
          val function = scriptScoreFunction(Script("params.agg1 + params.agg2 > 10"))
            .weight(2.0)
            .filter(matches("field", "value"))

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
        test("weightFunction") {
          val function = weightFunction(10.0).filter(matches("field", "value"))

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
