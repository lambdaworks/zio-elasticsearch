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
import zio.elasticsearch.Field
import zio.elasticsearch.query.DecayFunctionType._
import zio.elasticsearch.script.Script
import zio.json.ast.Json
import zio.json.ast.Json.{Num, Obj, Str}
import zio.schema.Schema

sealed trait FunctionScoreFunction[S] {

  val filter: Option[ElasticQuery[S]]

  /**
   * Sets the `filter` parameter for the [[zio.elasticsearch.query.FunctionScoreFunction]].
   *
   * @param filter
   *   the [[zio.elasticsearch.query.ElasticQuery]] object representing the query that is going to be used for
   *   filtering.
   * @return
   *   an instance of [[zio.elasticsearch.query.FunctionScoreFunction]] that represents the function score query to be
   *   performed enriched with `filter` parameter
   */
  def filter(filter: ElasticQuery[Any]): FunctionScoreFunction[S]

  /**
   * Sets the `filter` parameter for the [[zio.elasticsearch.query.FunctionScoreFunction]].
   *
   * @param filter
   *   the [[zio.elasticsearch.query.ElasticQuery]] object representing the query that is going to be used for
   *   filtering.
   * @tparam S1
   *
   * @return
   *   a type-safe instance of [[zio.elasticsearch.query.FunctionScoreFunction]] that represents the function score
   *   query to be performed enriched with `filter` parameter
   */
  def filter[S1 <: S: Schema](filter: ElasticQuery[S1]): FunctionScoreFunction[S1]

  private[elasticsearch] def toJson: Json
}

object FunctionScoreFunction {

  /**
   * Constructs a type-safe instance of [[zio.elasticsearch.query.DecayFunction]] with `decayFunctionType` of
   * [[zio.elasticsearch.query.DecayFunctionType.Exp]] using the specified parameters.
   * [[zio.elasticsearch.query.DecayFunction]] is used to score a document with a function that decays depending on the
   * distance of a numeric field value of the document from a user given origin. This is similar to a range query, but
   * with smooth edges instead of boxes.
   *
   * @param field
   *   the type-safe field for which query is specified for
   * @param origin
   *   the point of origin used for calculating distance
   * @param scale
   *   defines the distance from origin + offset at which the computed score will equal `decay` parameter
   * @return
   *   an instance of [[zio.elasticsearch.query.DecayFunction]] that represents the function score query to be
   *   performed.
   */
  def expDecayFunction[S](field: Field[S, _], origin: String, scale: String): DecayFunction[S] =
    DecayFunction[S](
      field = field.toString,
      decayFunctionType = Exp,
      origin = origin,
      scale = scale,
      decay = None,
      filter = None,
      multiValueMode = None,
      offset = None,
      weight = None
    )

  /**
   * Constructs an instance of [[zio.elasticsearch.query.DecayFunction]] with `decayFunctionType` of
   * [[zio.elasticsearch.query.DecayFunctionType.Exp]] using the specified parameters.
   * [[zio.elasticsearch.query.DecayFunction]] is used to score a document with a function that decays depending on the
   * distance of a numeric field value of the document from a user given origin. This is similar to a range query, but
   * with smooth edges instead of boxes.
   *
   * @param field
   *   the field for which query is specified for
   * @param origin
   *   the point of origin used for calculating distance
   * @param scale
   *   defines the distance from origin + offset at which the computed score will equal `decay`
   * @return
   *   an instance of [[zio.elasticsearch.query.DecayFunction]] that represents the function score query to be
   *   performed.
   */
  def expDecayFunction(field: String, origin: String, scale: String): DecayFunction[Any] =
    DecayFunction[Any](
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

  /**
   * Constructs a type-safe instance of [[zio.elasticsearch.query.FieldValueFactor]] using the specified parameters.
   * [[zio.elasticsearch.query.FieldValueFactor]] function allows you to use a field from a document to influence the
   * score. It is similar to using the script_score function, however, it avoids the overhead of scripting. If used on a
   * multi-valued field, only the first value of the field is used in calculations.
   *
   * @param field
   *   the type-safe field to be extracted from the document
   * @return
   *   an instance of [[zio.elasticsearch.query.FieldValueFactor]] that represents the function score query to be
   *   performed.
   */
  def fieldValueFactor[S](field: Field[S, _]): FieldValueFactor[S] =
    FieldValueFactor[S](
      field = field.toString,
      factor = None,
      filter = None,
      modifier = None,
      missing = None,
      weight = None
    )

  /**
   * Constructs an instance of [[zio.elasticsearch.query.FieldValueFactor]] using the specified parameters.
   * [[zio.elasticsearch.query.FieldValueFactor]] function allows you to use a field from a document to influence the
   * score. It is similar to using the script_score function, however, it avoids the overhead of scripting. If used on a
   * multi-valued field, only the first value of the field is used in calculations.
   *
   * @param field
   *   the type-safe field to be extracted from the document
   * @return
   *   an instance of [[zio.elasticsearch.query.FieldValueFactor]] that represents the function score query to be
   *   performed.
   */
  def fieldValueFactor(field: String): FieldValueFactor[Any] =
    FieldValueFactor[Any](
      field = field,
      factor = None,
      filter = None,
      modifier = None,
      missing = None,
      weight = None
    )

  /**
   * Constructs a type-safe instance of [[zio.elasticsearch.query.DecayFunction]] with `decayFunctionType` of
   * [[zio.elasticsearch.query.DecayFunctionType.Gauss]] using the specified parameters.
   * [[zio.elasticsearch.query.DecayFunction]] is used to score a document with a function that decays depending on the
   * distance of a numeric field value of the document from a user given origin. This is similar to a range query, but
   * with smooth edges instead of boxes.
   *
   * @param field
   *   the type-safe field for which query is specified for
   * @param origin
   *   the point of origin used for calculating distance
   * @param scale
   *   defines the distance from origin + offset at which the computed score will equal `decay` parameter
   * @return
   *   an instance of [[zio.elasticsearch.query.DecayFunction]] that represents the function score query to be
   *   performed.
   */
  def gaussDecayFunction[S](field: Field[S, _], origin: String, scale: String): DecayFunction[S] =
    DecayFunction[S](
      field = field.toString,
      decayFunctionType = Gauss,
      origin = origin,
      scale = scale,
      decay = None,
      filter = None,
      multiValueMode = None,
      offset = None,
      weight = None
    )

  /**
   * Constructs an instance of [[zio.elasticsearch.query.DecayFunction]] with `decayFunctionType` of
   * [[zio.elasticsearch.query.DecayFunctionType.Gauss]] using the specified parameters.
   * [[zio.elasticsearch.query.DecayFunction]] is used to score a document with a function that decays depending on the
   * distance of a numeric field value of the document from a user given origin. This is similar to a range query, but
   * with smooth edges instead of boxes.
   *
   * @param field
   *   the field for which query is specified for
   * @param origin
   *   the point of origin used for calculating distance
   * @param scale
   *   defines the distance from origin + offset at which the computed score will equal `decay`
   * @return
   *   an instance of [[zio.elasticsearch.query.DecayFunction]] that represents the function score query to be
   *   performed.
   */
  def gaussDecayFunction(field: String, origin: String, scale: String): DecayFunction[Any] =
    DecayFunction[Any](
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

  /**
   * Constructs a type-safe instance of [[zio.elasticsearch.query.DecayFunction]] with `decayFunctionType` of
   * [[zio.elasticsearch.query.DecayFunctionType.Linear]] using the specified parameters.
   * [[zio.elasticsearch.query.DecayFunction]] is used to score a document with a function that decays depending on the
   * distance of a numeric field value of the document from a user given origin. This is similar to a range query, but
   * with smooth edges instead of boxes.
   *
   * @param field
   *   the type-safe field for which query is specified for
   * @param origin
   *   the point of origin used for calculating distance
   * @param scale
   *   defines the distance from origin + offset at which the computed score will equal `decay` parameter
   * @return
   *   an instance of [[zio.elasticsearch.query.DecayFunction]] that represents the function score query to be
   *   performed.
   */
  def linearDecayFunction[S](field: Field[S, _], origin: String, scale: String): DecayFunction[S] =
    DecayFunction[S](
      field = field.toString,
      decayFunctionType = Linear,
      origin = origin,
      scale = scale,
      decay = None,
      filter = None,
      multiValueMode = None,
      offset = None,
      weight = None
    )

  /**
   * Constructs an instance of [[zio.elasticsearch.query.DecayFunction]] with `decayFunctionType` of
   * [[zio.elasticsearch.query.DecayFunctionType.Exp]] using the specified parameters.
   * [[zio.elasticsearch.query.DecayFunction]] is used to score a document with a function that decays depending on the
   * distance of a numeric field value of the document from a user given origin. This is similar to a range query, but
   * with smooth edges instead of boxes.
   *
   * @param field
   *   the field for which query is specified for
   * @param origin
   *   the point of origin used for calculating distance
   * @param scale
   *   defines the distance from origin + offset at which the computed score will equal `decay`
   * @return
   *   an instance of [[zio.elasticsearch.query.DecayFunction]] that represents the function score query to be
   *   performed.
   */
  def linearDecayFunction(field: String, origin: String, scale: String): DecayFunction[Any] =
    DecayFunction[Any](
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

  /**
   * Constructs an instance of [[zio.elasticsearch.query.RandomScoreFunction]] using the specified parameters.
   * [[zio.elasticsearch.query.RandomScoreFunction]] generates scores that are uniformly distributed from 0 up to but
   * not including 1. By default, it uses the internal Lucene doc ids as a source of randomness, which is very efficient
   * but unfortunately not reproducible since documents might be renumbered by merges.
   *
   * @return
   *   an instance of [[zio.elasticsearch.query.RandomScoreFunction]] that represents the function score query to be
   *   performed.
   */
  def randomScoreFunction(): RandomScoreFunction[Any] =
    RandomScoreFunction[Any](filter = None, seedAndField = None, weight = None)

  /**
   * Constructs an instance of [[zio.elasticsearch.query.RandomScoreFunction]] using the specified parameters.
   * [[zio.elasticsearch.query.RandomScoreFunction]] generates scores that are uniformly distributed from 0 up to but
   * not including 1. By default, it uses the internal Lucene doc ids as a source of randomness, which is very efficient
   * but unfortunately not reproducible since documents might be renumbered by merges.
   *
   * @param seed
   *   the final score will be computed based on this value and default value for `field` which is "_seq_no"
   * @return
   *   an instance of [[zio.elasticsearch.query.RandomScoreFunction]] that represents the function score query to be
   *   performed.
   */
  def randomScoreFunction(seed: Long): RandomScoreFunction[Any] =
    RandomScoreFunction[Any](filter = None, seedAndField = Some(SeedAndField(seed = seed)), weight = None)

  /**
   * Constructs an instance of [[zio.elasticsearch.query.RandomScoreFunction]] using the specified parameters.
   * [[zio.elasticsearch.query.RandomScoreFunction]] generates scores that are uniformly distributed from 0 up to but
   * not including 1. By default, it uses the internal Lucene doc ids as a source of randomness, which is very efficient
   * but unfortunately not reproducible since documents might be renumbered by merges.
   *
   * @param seed
   *   the final score will be computed based on this value and value for `field`
   * @param field
   *   the field value that will be used to compute final score
   * @return
   *   an instance of [[zio.elasticsearch.query.RandomScoreFunction]] that represents the function score query to be
   *   performed.
   */
  def randomScoreFunction(seed: Long, field: String): RandomScoreFunction[Any] =
    RandomScoreFunction[Any](
      filter = None,
      seedAndField = Some(SeedAndField(seed = seed, fieldName = field)),
      weight = None
    )

  /**
   * Constructs an instance of [[zio.elasticsearch.query.ScriptScoreFunction]] using the specified parameters.
   * [[zio.elasticsearch.query.ScriptScoreFunction]] function allows you to wrap another query and customize the scoring
   * of it optionally with a computation derived from other numeric field values in the doc using a script expression.
   *
   * @param script
   *   the [[zio.elasticsearch.script.Script]] that will be used to calculate score
   * @return
   *   an instance of [[zio.elasticsearch.query.ScriptScoreFunction]] that represents the function score query to be
   *   performed.
   */
  def scriptScoreFunction(script: Script): ScriptScoreFunction[Any] =
    ScriptScoreFunction[Any](script = script, weight = None, filter = None)

  /**
   * Constructs an instance of [[zio.elasticsearch.query.WeightFunction]] using the specified parameters.
   * [[zio.elasticsearch.query.WeightFunction]] score allows you to multiply the score by the provided weight.
   *
   * @param weight
   *   the number you wish to multiply the score with
   * @return
   *   an instance of [[zio.elasticsearch.query.WeightFunction]] that represents the function score query to be
   *   performed.
   */
  def weightFunction(weight: Double): WeightFunction[Any] =
    WeightFunction[Any](weight = weight, filter = None)

}

final case class DecayFunction[S](
  field: String,
  decayFunctionType: DecayFunctionType,
  origin: String,
  scale: String,
  decay: Option[Double],
  filter: Option[ElasticQuery[S]],
  multiValueMode: Option[MultiValueMode],
  offset: Option[String],
  weight: Option[Double]
) extends FunctionScoreFunction[S] { self =>

  /**
   * Sets the `decay` parameter for the [[zio.elasticsearch.query.DecayFunction]]. It defines how documents are scored
   * at the distance given at scale. If no decay is defined, documents at the distance scale will be scored 0.5.
   *
   * @param value
   *   the [[Double]] value for `decay` parameter
   * @return
   *   a new instance of the [[zio.elasticsearch.query.DecayFunction]] enriched with the `decay` parameter.
   */
  def decay(value: Double): DecayFunction[S] =
    self.copy(decay = Some(value))

  def filter(filter: ElasticQuery[Any]): FunctionScoreFunction[S] =
    self.copy(filter = Some(filter))

  def filter[S1 <: S: Schema](filter: ElasticQuery[S1]): DecayFunction[S1] =
    DecayFunction[S1](
      field = self.field,
      decayFunctionType = self.decayFunctionType,
      origin = self.origin,
      scale = self.scale,
      decay = self.decay,
      filter = Some(filter),
      multiValueMode = self.multiValueMode,
      offset = self.offset,
      weight = self.weight
    )

  /**
   * Sets the `multiValueMode` parameter for the [[zio.elasticsearch.query.DecayFunction]]. If a field used for
   * computing the decay contains multiple values, per default the value closest to the origin is chosen for determining
   * the distance. This can be changed by setting `multiValueMode`.
   *
   * @param value
   *   the [[zio.elasticsearch.query.MultiValueMode]] value for `multiValueMode` parameter, it can have following
   *   values:
   *   - [[zio.elasticsearch.query.MultiValueMode.Avg]]
   *   - [[zio.elasticsearch.query.MultiValueMode.Max]]
   *   - [[zio.elasticsearch.query.MultiValueMode.Median]]
   *   - [[zio.elasticsearch.query.MultiValueMode.Min]]
   *   - [[zio.elasticsearch.query.MultiValueMode.Sum]]
   * @return
   *   a new instance of the [[zio.elasticsearch.query.DecayFunction]] enriched with the `multiValueMode` parameter.
   */
  def multiValueMode(value: MultiValueMode): DecayFunction[S] =
    self.copy(multiValueMode = Some(value))

  /**
   * Sets the `offset` parameter for the [[zio.elasticsearch.query.DecayFunction]]. If an offset is defined, the decay
   * function will only compute the decay function for documents with a distance greater than the defined offset. The
   * default is 0.
   *
   * @param value
   *   the [[String]] value for `offset` parameter
   * @return
   *   a new instance of the [[zio.elasticsearch.query.DecayFunction]] enriched with the `offset` parameter.
   */
  def offset(value: String): DecayFunction[S] =
    self.copy(offset = Some(value))

  /**
   * Sets the `weight` parameter for the [[zio.elasticsearch.query.DecayFunction]]. The weight score allows you to
   * multiply the score by the provided weight. This can sometimes be desired since boost value set on specific queries
   * gets normalized, while for this score function it does not.
   *
   * @param value
   *   the [[Double]] value for `weight` parameter
   * @return
   *   a new instance of the [[zio.elasticsearch.query.DecayFunction]] enriched with the `weight` parameter.
   */
  def weight(value: Double): DecayFunction[S] =
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

final case class FieldValueFactor[S](
  field: String,
  factor: Option[Double],
  filter: Option[ElasticQuery[S]],
  modifier: Option[FieldValueFactorFunctionModifier],
  missing: Option[Double],
  weight: Option[Double]
) extends FunctionScoreFunction[S] { self =>

  /**
   * Sets the `factor` parameter for the [[zio.elasticsearch.query.FieldValueFactor]]. Optional factor to multiply the
   * field value with, defaults to 1.
   *
   * @param value
   *   the [[Double]] value for `factor` parameter
   * @return
   *   a new instance of the [[zio.elasticsearch.query.FieldValueFactor]] enriched with the `factor` parameter.
   */
  def factor(value: Double): FieldValueFactor[S] =
    self.copy(factor = Some(value))

  /**
   * Sets the `modifier` parameter for the [[zio.elasticsearch.query.FieldValueFactor]]. Modifier to apply to the field
   * value. Defaults to none.
   *
   * @param value
   *   the [[zio.elasticsearch.query.FieldValueFactorFunctionModifier]] value for `modifier` parameter, it can be:
   *   - [[zio.elasticsearch.query.FieldValueFactorFunctionModifier.Ln]]
   *   - [[zio.elasticsearch.query.FieldValueFactorFunctionModifier.Ln1p]]
   *   - [[zio.elasticsearch.query.FieldValueFactorFunctionModifier.Ln2p]]
   *   - [[zio.elasticsearch.query.FieldValueFactorFunctionModifier.Log]]
   *   - [[zio.elasticsearch.query.FieldValueFactorFunctionModifier.Log1p]]
   *   - [[zio.elasticsearch.query.FieldValueFactorFunctionModifier.Log2p]]
   *   - [[zio.elasticsearch.query.FieldValueFactorFunctionModifier.None]]
   *   - [[zio.elasticsearch.query.FieldValueFactorFunctionModifier.Reciprocal]]
   *   - [[zio.elasticsearch.query.FieldValueFactorFunctionModifier.Sqrt]]
   *   - [[zio.elasticsearch.query.FieldValueFactorFunctionModifier.Square]]
   * @return
   *   a new instance of the [[zio.elasticsearch.query.FieldValueFactor]] enriched with the `modifier` parameter.
   */
  def modifier(value: FieldValueFactorFunctionModifier): FieldValueFactor[S] =
    self.copy(modifier = Some(value))

  /**
   * Sets the `missing` parameter for the [[zio.elasticsearch.query.FieldValueFactor]]. Value used if the document does
   * not have that field. The modifier and factor are still applied to it as though it were read from the document.
   *
   * @param value
   *   the [[Double]] value for `missing` parameter
   *
   * @return
   *   a new instance of the [[zio.elasticsearch.query.FieldValueFactor]] enriched with the `missing` parameter.
   */
  def missing(value: Double): FieldValueFactor[S] =
    self.copy(missing = Some(value))

  /**
   * Sets the `weight` parameter for the [[zio.elasticsearch.query.FieldValueFactor]]. The weight score allows you to
   * multiply the score by the provided weight. This can sometimes be desired since boost value set on specific queries
   * gets normalized, while for this score function it does not.
   *
   * @param value
   *   the [[Double]] value for `weight` parameter
   * @return
   *   a new instance of the [[zio.elasticsearch.query.FieldValueFactor]] enriched with the `weight` parameter.
   */
  def weight(value: Double): FieldValueFactor[S] =
    self.copy(weight = Some(value))

  def filter(value: ElasticQuery[Any]): FieldValueFactor[S] =
    self.copy(filter = Some(value))

  def filter[S1 <: S: Schema](filter: ElasticQuery[S1]): FieldValueFactor[S1] =
    FieldValueFactor[S1](
      field = self.field,
      factor = self.factor,
      filter = Some(filter),
      modifier = self.modifier,
      missing = self.missing,
      weight = self.weight
    )

  private[elasticsearch] def toJson: Json =
    Obj(
      Chunk(
        Some(
          "field_value_factor" -> Obj(
            Chunk(
              Some("field" -> Str(field)),
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

private[elasticsearch] final case class RandomScoreFunction[S](
  filter: Option[ElasticQuery[S]],
  seedAndField: Option[SeedAndField],
  weight: Option[Double]
) extends FunctionScoreFunction[S] { self =>

  /**
   * Sets the `weight` parameter for the [[zio.elasticsearch.query.ScriptScoreFunction]]. The weight score allows you to
   * multiply the score by the provided weight. This can sometimes be desired since boost value set on specific queries
   * gets normalized, while for this score function it does not.
   *
   * @param value
   *   the [[Double]] value for `weight` parameter
   * @return
   *   a new instance of the [[zio.elasticsearch.query.ScriptScoreFunction]] enriched with the `weight` parameter.
   */
  def weight(value: Double): RandomScoreFunction[S] =
    self.copy(weight = Some(value))

  def filter(filter: ElasticQuery[Any]): RandomScoreFunction[S] =
    self.copy(filter = Some(filter))

  def filter[S1 <: S: Schema](filter: ElasticQuery[S1]): RandomScoreFunction[S1] =
    RandomScoreFunction[S1](Some(filter), seedAndField, weight)

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

private[elasticsearch] final case class ScriptScoreFunction[S](
  script: Script,
  filter: Option[ElasticQuery[S]],
  weight: Option[Double]
) extends FunctionScoreFunction[S] { self =>

  /**
   * Sets the `weight` parameter for the [[zio.elasticsearch.query.RandomScoreFunction]]. The weight score allows you to
   * multiply the score by the provided weight. This can sometimes be desired since boost value set on specific queries
   * gets normalized, while for this score function it does not.
   *
   * @param value
   *   the [[Double]] value for `weight` parameter
   * @return
   *   a new instance of the [[zio.elasticsearch.query.RandomScoreFunction]] enriched with the `weight` parameter.
   */
  def weight(value: Double): ScriptScoreFunction[S] =
    self.copy(weight = Some(value))

  def filter(filter: ElasticQuery[Any]): ScriptScoreFunction[S] =
    self.copy(filter = Some(filter))

  def filter[S1 <: S: Schema](filter: ElasticQuery[S1]): FunctionScoreFunction[S1] =
    ScriptScoreFunction[S1](
      script = script,
      filter = Some(filter),
      weight = weight
    )

  private[elasticsearch] def toJson: Json =
    Obj(
      Chunk(
        Some("script_score" -> Obj("script" -> script.toJson)),
        weight.map("weight" -> Num(_)),
        filter.map(f => "filter" -> f.toJson(None))
      ).flatten
    )

}

private[elasticsearch] final case class WeightFunction[S](weight: Double, filter: Option[ElasticQuery[S]])
    extends FunctionScoreFunction[S] { self =>
  def filter(filter: ElasticQuery[Any]): FunctionScoreFunction[S] =
    self.copy(filter = Some(filter))

  def filter[S1 <: S: Schema](filter: ElasticQuery[S1]): FunctionScoreFunction[S1] =
    WeightFunction[S1](weight = weight, filter = Some(filter))

  private[elasticsearch] def toJson: Json =
    Obj(
      Chunk(
        Some("weight" -> Num(weight)),
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
  case object Ln         extends FieldValueFactorFunctionModifier
  case object Ln1p       extends FieldValueFactorFunctionModifier
  case object Ln2p       extends FieldValueFactorFunctionModifier
  case object Log        extends FieldValueFactorFunctionModifier
  case object Log1p      extends FieldValueFactorFunctionModifier
  case object Log2p      extends FieldValueFactorFunctionModifier
  case object None       extends FieldValueFactorFunctionModifier
  case object Reciprocal extends FieldValueFactorFunctionModifier
  case object Sqrt       extends FieldValueFactorFunctionModifier
  case object Square     extends FieldValueFactorFunctionModifier
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
