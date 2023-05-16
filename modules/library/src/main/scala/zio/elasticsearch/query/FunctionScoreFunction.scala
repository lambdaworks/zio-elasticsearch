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
import zio.elasticsearch.script.Script
import zio.json.ast.Json
import zio.json.ast.Json.{Num, Obj, Str}

sealed trait FunctionScoreFunction {
  val filter: Option[ElasticQuery[_]]
  def withFilter(filter: ElasticQuery[_]): FunctionScoreFunction
  private[elasticsearch] def toJson: Json
}

final case class ScriptScoreFunction(script: Script, weight: Option[Double], filter: Option[ElasticQuery[_]])
    extends FunctionScoreFunction { self =>
  def weight(value: Double): ScriptScoreFunction = self.copy(weight = Some(value))

  def withFilter(value: ElasticQuery[_]): ScriptScoreFunction = self.copy(filter = Some(value))

  private[elasticsearch] def toJson: Json =
    Obj(
      Chunk(
        Some("script_score" -> Obj("script" -> script.toJson)),
        weight.map("weight" -> Num(_)),
        filter.map(f => "filter" -> f.paramsToJson(None))
      ).flatten
    )

}

final case class WeightFunction(weight: Double, filter: Option[ElasticQuery[_]]) extends FunctionScoreFunction { self =>
  def withFilter(value: ElasticQuery[_]): WeightFunction = self.copy(filter = Some(value))

  private[elasticsearch] def toJson: Json =
    Obj(
      Chunk(
        Some("weight" -> Num(weight)),
        filter.map(f => "filter" -> f.paramsToJson(None))
      ).flatten
    )
}

final case class RandomScoreFunction(
  seedAndField: Option[SeedAndField],
  weight: Option[Double],
  filter: Option[ElasticQuery[_]]
) extends FunctionScoreFunction { self =>

  def weight(value: Double): RandomScoreFunction = self.copy(weight = Some(value))

  def withFilter(value: ElasticQuery[_]): RandomScoreFunction = self.copy(filter = Some(value))

  private[elasticsearch] def toJson: Json =
    Obj(
      Chunk(
        Some(
          "random_score" -> seedAndField.fold(Obj())(sf => Obj("seed" -> Num(sf.seed), "field" -> Str(sf.fieldName)))
        ),
        weight.map("weight" -> Num(_)),
        filter.map(f => "filter" -> f.paramsToJson(None))
      ).flatten
    )
}

final case class FieldValueFactor(
  fieldName: String,
  factor: Option[Double],
  filter: Option[ElasticQuery[_]],
  modifier: Option[FieldValueFactorFunctionModifier],
  missing: Option[Double]
) extends FunctionScoreFunction { self =>

  def factor(value: Double): FieldValueFactor = self.copy(factor = Some(value))

  def modifier(value: FieldValueFactorFunctionModifier): FieldValueFactor = self.copy(modifier = Some(value))

  def missing(value: Double): FieldValueFactor = self.copy(missing = Some(value))

  def withFilter(value: ElasticQuery[_]): FieldValueFactor = self.copy(filter = Some(value))

  override def toJson: Json =
    Obj(
      "field_value_factor" -> Obj(
        Chunk(
          Some("field" -> Str(fieldName)),
          factor.map("factor" -> Num(_)),
          filter.map(f => "filter" -> f.toJson),
          modifier.map(m => "modifier" -> Str(m.toString.toLowerCase)),
          missing.map("missing" -> Num(_))
        ).flatten
      )
    )
}

sealed trait FieldValueFactorFunctionModifier

object FieldValueFactorFunctionModifier {
  case object NONE       extends FieldValueFactorFunctionModifier
  case object LOG        extends FieldValueFactorFunctionModifier
  case object LOG1P      extends FieldValueFactorFunctionModifier
  case object LOG2P      extends FieldValueFactorFunctionModifier
  case object LN         extends FieldValueFactorFunctionModifier
  case object LN1P       extends FieldValueFactorFunctionModifier
  case object LN2P       extends FieldValueFactorFunctionModifier
  case object SQUARE     extends FieldValueFactorFunctionModifier
  case object SQRT       extends FieldValueFactorFunctionModifier
  case object RECIPROCAL extends FieldValueFactorFunctionModifier
}
final case class DecayFunction(
  field: String,
  decayFunctionType: DecayFunctionType,
  origin: String,
  scale: String,
  offset: Option[String],
  decay: Option[Double],
  weight: Option[Double],
  multiValueMode: Option[MultiValueMode],
  override val filter: Option[ElasticQuery[_]]
) extends FunctionScoreFunction { self =>

  def offset(value: String): DecayFunction = self.copy(offset = Some(value))

  def decay(value: Double): DecayFunction = self.copy(decay = Some(value))

  def weight(value: Double): DecayFunction = self.copy(weight = Some(value))

  def multiValueMode(value: MultiValueMode): DecayFunction = self.copy(multiValueMode = Some(value))

  override def withFilter(filter: ElasticQuery[_]): FunctionScoreFunction = self.copy(filter = Some(filter))

  override def toJson: Json =
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
        weight.map("weight" -> Num(_))
      ).flatten
    )
}

sealed trait MultiValueMode

object MultiValueMode {
  case object Avg    extends MultiValueMode
  case object Min    extends MultiValueMode
  case object Max    extends MultiValueMode
  case object Sum    extends MultiValueMode
  case object Median extends MultiValueMode
}

sealed trait DecayFunctionType

object DecayFunctionType {
  case object Gauss  extends DecayFunctionType
  case object Linear extends DecayFunctionType
  case object Exp    extends DecayFunctionType
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

sealed trait FunctionScoreBoostMode

object FunctionScoreBoostMode {
  case object Avg      extends FunctionScoreBoostMode
  case object Max      extends FunctionScoreBoostMode
  case object Min      extends FunctionScoreBoostMode
  case object Multiply extends FunctionScoreBoostMode
  case object Replace  extends FunctionScoreBoostMode
  case object Sum      extends FunctionScoreScoreMode
}

final case class SeedAndField(seed: Long, fieldName: String = SeedAndField.DefaultFieldName)
object SeedAndField {
  final val DefaultFieldName = "_seq_no"
}
