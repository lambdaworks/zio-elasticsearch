/*
 * Copyright 2022 LambdaWorks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package zio.elasticsearch.query

import zio.Chunk
import zio.elasticsearch.query.DecayFunctionType._
import zio.elasticsearch.script.Script
import zio.json.ast.Json
import zio.json.ast.Json.{Num, Obj, Str}

sealed trait FunctionScoreFunction {

  val filter: Option[ElasticQuery[_]]

  def filter(filter: ElasticQuery[_]): FunctionScoreFunction

  private[elasticsearch] def toJson: Json
}

object FunctionScoreFunction {
  def expDecayFunction(field: String, origin: String, scale: String): DecayFunction =
    DecayFunction(
      field = field,
      decayFunctionType = Exp,
      origin = origin,
      scale = scale,
      decay = None,
      filter = None,
      multiValueMode = None,
      offset = None,
      weight = None
    )

  def fieldValueFactor(field: String): FieldValueFactor =
    FieldValueFactor(
      fieldName = field,
      factor = None,
      filter = None,
      modifier = None,
      missing = None,
      weight = None
    )

  def gaussDecayFunction(field: String, origin: String, scale: String): DecayFunction =
    DecayFunction(
      field = field,
      decayFunctionType = Gauss,
      origin = origin,
      scale = scale,
      decay = None,
      filter = None,
      multiValueMode = None,
      offset = None,
      weight = None
    )

  def linearDecayFunction(field: String, origin: String, scale: String): DecayFunction =
    DecayFunction(
      field = field,
      decayFunctionType = Linear,
      origin = origin,
      scale = scale,
      decay = None,
      filter = None,
      multiValueMode = None,
      offset = None,
      weight = None
    )

  def randomScoreFunction(): RandomScoreFunction =
    RandomScoreFunction(filter = None, seedAndField = None, weight = None)

  def randomScoreFunction(seed: Long): RandomScoreFunction =
    RandomScoreFunction(filter = None, seedAndField = Some(SeedAndField(seed = seed)), weight = None)

  def randomScoreFunction(seed: Long, field: String): RandomScoreFunction =
    RandomScoreFunction(filter = None, seedAndField = Some(SeedAndField(seed = seed, fieldName = field)), weight = None)

  def scriptScoreFunction(script: Script): ScriptScoreFunction =
    ScriptScoreFunction(script = script, weight = None, filter = None)

  def weightFunction(weight: Double): WeightFunction =
    WeightFunction(weight = weight, filter = None)
}

private[elasticsearch] final case class ScriptScoreFunction(
  script: Script,
  filter: Option[ElasticQuery[_]],
  weight: Option[Double]
) extends FunctionScoreFunction { self =>
  def weight(value: Double): ScriptScoreFunction =
    self.copy(weight = Some(value))

  def filter(value: ElasticQuery[_]): ScriptScoreFunction =
    self.copy(filter = Some(value))

  private[elasticsearch] def toJson: Json =
    Obj(
      Chunk(
        Some("script_score" -> Obj("script" -> script.toJson)),
        weight.map("weight" -> Num(_)),
        filter.map(f => "filter" -> f.toJson(None))
      ).flatten
    )

}

private[elasticsearch] final case class WeightFunction(weight: Double, filter: Option[ElasticQuery[_]])
    extends FunctionScoreFunction { self =>
  def filter(value: ElasticQuery[_]): WeightFunction =
    self.copy(filter = Some(value))

  private[elasticsearch] def toJson: Json =
    Obj(
      Chunk(
        Some("weight" -> Num(weight)),
        filter.map(f => "filter" -> f.toJson(None))
      ).flatten
    )
}

private[elasticsearch] final case class RandomScoreFunction(
  filter: Option[ElasticQuery[_]],
  seedAndField: Option[SeedAndField],
  weight: Option[Double]
) extends FunctionScoreFunction { self =>

  def weight(value: Double): RandomScoreFunction =
    self.copy(weight = Some(value))

  def filter(value: ElasticQuery[_]): RandomScoreFunction =
    self.copy(filter = Some(value))

  private[elasticsearch] def toJson: Json =
    Obj(
      Chunk(
        Some(
          "random_score" -> seedAndField.fold(Obj())(sf => Obj("seed" -> Num(sf.seed), "field" -> Str(sf.fieldName)))
        ),
        weight.map("weight" -> Num(_)),
        filter.map(f => "filter" -> f.toJson(None))
      ).flatten
    )
}

final case class FieldValueFactor(
  fieldName: String,
  factor: Option[Double],
  filter: Option[ElasticQuery[_]],
  modifier: Option[FieldValueFactorFunctionModifier],
  missing: Option[Double],
  weight: Option[Double]
) extends FunctionScoreFunction { self =>

  def factor(value: Double): FieldValueFactor =
    self.copy(factor = Some(value))

  def modifier(value: FieldValueFactorFunctionModifier): FieldValueFactor =
    self.copy(modifier = Some(value))

  def missing(value: Double): FieldValueFactor =
    self.copy(missing = Some(value))

  def weight(value: Double): FieldValueFactor =
    self.copy(weight = Some(value))

  def filter(value: ElasticQuery[_]): FieldValueFactor =
    self.copy(filter = Some(value))

  private[elasticsearch] def toJson: Json =
    Obj(
      Chunk(
        Some(
          "field_value_factor" -> Obj(
            Chunk(
              Some("field" -> Str(fieldName)),
              factor.map("factor" -> Num(_)),
              modifier.map(m => "modifier" -> Str(m.toString.toLowerCase)),
              missing.map("missing" -> Num(_))
            ).flatten
          )
        ),
        filter.map(f => "filter" -> f.toJson(None)),
        weight.map("weight" -> Num(_))
      ).flatten
    )
}

final case class DecayFunction(
  field: String,
  decayFunctionType: DecayFunctionType,
  origin: String,
  scale: String,
  decay: Option[Double],
  filter: Option[ElasticQuery[_]],
  multiValueMode: Option[MultiValueMode],
  offset: Option[String],
  weight: Option[Double]
) extends FunctionScoreFunction { self =>

  def decay(value: Double): DecayFunction =
    self.copy(decay = Some(value))

  def filter(filter: ElasticQuery[_]): FunctionScoreFunction =
    self.copy(filter = Some(filter))

  def multiValueMode(value: MultiValueMode): DecayFunction =
    self.copy(multiValueMode = Some(value))

  def offset(value: String): DecayFunction =
    self.copy(offset = Some(value))

  def weight(value: Double): DecayFunction =
    self.copy(weight = Some(value))

  private[elasticsearch] def toJson: Json =
    Obj(
      Chunk(
        Some(
          s"${decayFunctionType.toString.toLowerCase}" -> Obj(
            Chunk(
              Some(
                s"$field" -> Obj(
                  Chunk(
                    Some("origin" -> Str(origin)),
                    Some("scale"  -> Str(scale)),
                    offset.map("offset" -> Str(_)),
                    decay.map("decay" -> Num(_))
                  ).flatten
                )
              ),
              multiValueMode.map(m => "multi_value_mode" -> Str(s"${m.toString.toLowerCase}"))
            ).flatten
          )
        ),
        weight.map("weight" -> Num(_)),
        filter.map(f => "filter" -> f.toJson(None))
      ).flatten
    )
}

sealed trait DecayFunctionType

object DecayFunctionType {
  case object Exp    extends DecayFunctionType
  case object Gauss  extends DecayFunctionType
  case object Linear extends DecayFunctionType
}

sealed trait FunctionScoreBoostMode

object FunctionScoreBoostMode {
  case object Avg      extends FunctionScoreBoostMode
  case object Max      extends FunctionScoreBoostMode
  case object Min      extends FunctionScoreBoostMode
  case object Multiply extends FunctionScoreBoostMode
  case object Replace  extends FunctionScoreBoostMode
  case object Sum      extends FunctionScoreScoreMode
}

sealed trait FunctionScoreScoreMode

object FunctionScoreScoreMode {
  case object Avg      extends FunctionScoreScoreMode
  case object First    extends FunctionScoreScoreMode
  case object Max      extends FunctionScoreScoreMode
  case object Min      extends FunctionScoreScoreMode
  case object Multiply extends FunctionScoreScoreMode
  case object None     extends FunctionScoreScoreMode
  case object Sum      extends FunctionScoreScoreMode
}

sealed trait FieldValueFactorFunctionModifier

object FieldValueFactorFunctionModifier {
  case object LN         extends FieldValueFactorFunctionModifier
  case object LN1P       extends FieldValueFactorFunctionModifier
  case object LN2P       extends FieldValueFactorFunctionModifier
  case object LOG        extends FieldValueFactorFunctionModifier
  case object LOG1P      extends FieldValueFactorFunctionModifier
  case object LOG2P      extends FieldValueFactorFunctionModifier
  case object NONE       extends FieldValueFactorFunctionModifier
  case object RECIPROCAL extends FieldValueFactorFunctionModifier
  case object SQRT       extends FieldValueFactorFunctionModifier
  case object SQUARE     extends FieldValueFactorFunctionModifier
}

sealed trait MultiValueMode

object MultiValueMode {
  case object Avg    extends MultiValueMode
  case object Max    extends MultiValueMode
  case object Median extends MultiValueMode
  case object Min    extends MultiValueMode
  case object Sum    extends MultiValueMode
}

private[elasticsearch] final case class SeedAndField(seed: Long, fieldName: String = SeedAndField.DefaultFieldName)
object SeedAndField {
  private final val DefaultFieldName = "_seq_no"
}
