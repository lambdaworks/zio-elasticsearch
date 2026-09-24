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
import zio.elasticsearch.ElasticPrimitive.ElasticPrimitiveOps
import zio.elasticsearch.Field
import zio.elasticsearch.query.options.{HasAnalyzer, HasIntervalFilter, HasUseField}
import zio.json.ast.Json
import zio.json.ast.Json.{Arr, Obj}
import zio.schema.Schema

sealed trait IntervalRule[-S] {
  private[elasticsearch] def toJson(fieldPath: Option[String]): Json
}

private[elasticsearch] object IntervalRule {
  def useFieldToJson(useField: Option[String], fieldPath: Option[String]): Option[(String, Json)] =
    useField.map(field => "use_field" -> fieldPath.foldRight(field)(_ + "." + _).toJson)
}

sealed trait IntervalAllOfRule[-S] extends IntervalRule[S] with HasIntervalFilter[IntervalAllOfRule, S] {

  /**
   * Sets the `max_gaps` parameter for this `all_of` interval rule, the maximum number of positions allowed between the
   * matching terms.
   *
   * @param g
   *   the maximum number of positions between the matching terms
   * @return
   *   a new instance of the interval rule with the `max_gaps` value set.
   */
  def maxGaps(g: Int): IntervalAllOfRule[S]

  /**
   * Requires that the matching terms appear in the order in which the rules are specified.
   *
   * @return
   *   a new instance of the interval rule with the `ordered` parameter set to `true`.
   */
  def orderedOn: IntervalAllOfRule[S]
}

private[elasticsearch] final case class IntervalAllOf[S](
  intervals: Chunk[IntervalRule[S]],
  maxGaps: Option[Int],
  ordered: Option[Boolean],
  filter: Option[IntervalFilter[S]]
) extends IntervalAllOfRule[S] { self =>

  def filter[S1 <: S](f: IntervalFilter[S1]): IntervalAllOfRule[S1] = self.copy[S1](filter = Some(f))

  def maxGaps(g: Int): IntervalAllOfRule[S] = self.copy(maxGaps = Some(g))

  def orderedOn: IntervalAllOfRule[S] = self.copy(ordered = Some(true))

  private[elasticsearch] def toJson(fieldPath: Option[String]): Json =
    Obj(
      "all_of" -> Obj(
        Chunk(
          Some("intervals" -> Arr(intervals.map(_.toJson(fieldPath)): _*)),
          maxGaps.map("max_gaps" -> _.toJson),
          ordered.map("ordered" -> _.toJson),
          filter.map("filter" -> _.toJson(fieldPath))
        ).flatten: _*
      )
    )
}

sealed trait IntervalAnyOfRule[-S] extends IntervalRule[S] with HasIntervalFilter[IntervalAnyOfRule, S]

private[elasticsearch] final case class IntervalAnyOf[S](
  intervals: Chunk[IntervalRule[S]],
  filter: Option[IntervalFilter[S]]
) extends IntervalAnyOfRule[S] { self =>

  def filter[S1 <: S](f: IntervalFilter[S1]): IntervalAnyOfRule[S1] = self.copy[S1](filter = Some(f))

  private[elasticsearch] def toJson(fieldPath: Option[String]): Json =
    Obj(
      "any_of" -> Obj(
        Chunk(
          Some("intervals" -> Arr(intervals.map(_.toJson(fieldPath)): _*)),
          filter.map("filter" -> _.toJson(fieldPath))
        ).flatten: _*
      )
    )
}

final case class IntervalFilter[-S](
  after: Option[IntervalRule[S]] = None,
  before: Option[IntervalRule[S]] = None,
  containedBy: Option[IntervalRule[S]] = None,
  containing: Option[IntervalRule[S]] = None,
  notContainedBy: Option[IntervalRule[S]] = None,
  notContaining: Option[IntervalRule[S]] = None,
  notOverlapping: Option[IntervalRule[S]] = None,
  overlapping: Option[IntervalRule[S]] = None,
  script: Option[zio.elasticsearch.script.Script] = None
) {
  private[elasticsearch] def toJson(fieldPath: Option[String]): Json =
    Obj(
      Chunk(
        after.map("after" -> _.toJson(fieldPath)),
        before.map("before" -> _.toJson(fieldPath)),
        containedBy.map("contained_by" -> _.toJson(fieldPath)),
        containing.map("containing" -> _.toJson(fieldPath)),
        notContainedBy.map("not_contained_by" -> _.toJson(fieldPath)),
        notContaining.map("not_containing" -> _.toJson(fieldPath)),
        notOverlapping.map("not_overlapping" -> _.toJson(fieldPath)),
        overlapping.map("overlapping" -> _.toJson(fieldPath)),
        script.map("script" -> _.toJson)
      ).flatten: _*
    )
}

sealed trait IntervalFuzzyRule[-S]
    extends IntervalRule[S]
    with HasAnalyzer[IntervalFuzzyRule[S]]
    with HasUseField[IntervalFuzzyRule, S] {

  /**
   * Sets the `prefix_length` parameter for this `fuzzy` interval rule, the number of leading characters that are not
   * considered for fuzzy matching.
   *
   * @param length
   *   the number of leading characters to keep unchanged
   * @return
   *   a new instance of the interval rule with the `prefix_length` value set.
   */
  def prefixLength(length: Int): IntervalFuzzyRule[S]

  /**
   * Sets the `fuzziness` parameter for this `fuzzy` interval rule, the maximum edit distance allowed for matching.
   *
   * @param value
   *   the maximum edit distance, either a number of edits (e.g. `"1"`) or `"AUTO"`
   * @return
   *   a new instance of the interval rule with the `fuzziness` value set.
   */
  def fuzziness(value: String): IntervalFuzzyRule[S]

  /**
   * Disables the `transpositions` parameter for this `fuzzy` interval rule, so that transposing two adjacent characters
   * is not treated as a single edit.
   *
   * @return
   *   a new instance of the interval rule with the `transpositions` parameter set to `false`.
   */
  def transpositionsDisabled: IntervalFuzzyRule[S]

  /**
   * Enables the `transpositions` parameter for this `fuzzy` interval rule, so that transposing two adjacent characters
   * is treated as a single edit.
   *
   * @return
   *   a new instance of the interval rule with the `transpositions` parameter set to `true`.
   */
  def transpositionsEnabled: IntervalFuzzyRule[S]
}

private[elasticsearch] final case class IntervalFuzzy[S](
  term: String,
  prefixLength: Option[Int],
  transpositions: Option[Boolean],
  fuzziness: Option[String],
  analyzer: Option[String],
  useField: Option[String]
) extends IntervalFuzzyRule[S] { self =>

  def analyzer(value: String): IntervalFuzzyRule[S] = self.copy(analyzer = Some(value))

  def fuzziness(value: String): IntervalFuzzyRule[S] = self.copy(fuzziness = Some(value))

  def prefixLength(length: Int): IntervalFuzzyRule[S] = self.copy(prefixLength = Some(length))

  def transpositionsDisabled: IntervalFuzzyRule[S] = self.copy(transpositions = Some(false))

  def transpositionsEnabled: IntervalFuzzyRule[S] = self.copy(transpositions = Some(true))

  def useField[S1 <: S: Schema](field: Field[S1, _]): IntervalFuzzyRule[S1] =
    self.copy[S1](useField = Some(field.toString))

  def useField(field: String): IntervalFuzzyRule[S] = self.copy(useField = Some(field))

  private[elasticsearch] def toJson(fieldPath: Option[String]): Json =
    Obj(
      "fuzzy" -> Obj(
        Chunk(
          Some("term" -> term.toJson),
          prefixLength.map("prefix_length" -> _.toJson),
          transpositions.map("transpositions" -> _.toJson),
          fuzziness.map("fuzziness" -> _.toJson),
          analyzer.map("analyzer" -> _.toJson),
          IntervalRule.useFieldToJson(useField, fieldPath)
        ).flatten: _*
      )
    )
}

sealed trait IntervalMatchRule[-S]
    extends IntervalRule[S]
    with HasAnalyzer[IntervalMatchRule[S]]
    with HasIntervalFilter[IntervalMatchRule, S]
    with HasUseField[IntervalMatchRule, S] {

  /**
   * Sets the `max_gaps` parameter for this `match` interval rule, the maximum number of positions allowed between the
   * matching terms.
   *
   * @param g
   *   the maximum number of positions between the matching terms
   * @return
   *   a new instance of the interval rule with the `max_gaps` value set.
   */
  def maxGaps(g: Int): IntervalMatchRule[S]

  /**
   * Requires that the matching terms appear in the order specified in the `query` string.
   *
   * @return
   *   a new instance of the interval rule with the `ordered` parameter set to `true`.
   */
  def orderedOn: IntervalMatchRule[S]
}

private[elasticsearch] final case class IntervalMatch[S](
  query: String,
  analyzer: Option[String],
  useField: Option[String],
  maxGaps: Option[Int],
  ordered: Option[Boolean],
  filter: Option[IntervalFilter[S]]
) extends IntervalMatchRule[S] { self =>

  def analyzer(value: String): IntervalMatchRule[S] = self.copy(analyzer = Some(value))

  def filter[S1 <: S](f: IntervalFilter[S1]): IntervalMatchRule[S1] = self.copy[S1](filter = Some(f))

  def maxGaps(g: Int): IntervalMatchRule[S] = self.copy(maxGaps = Some(g))

  def orderedOn: IntervalMatchRule[S] = self.copy(ordered = Some(true))

  def useField[S1 <: S: Schema](field: Field[S1, _]): IntervalMatchRule[S1] =
    self.copy[S1](useField = Some(field.toString))

  def useField(field: String): IntervalMatchRule[S] = self.copy(useField = Some(field))

  private[elasticsearch] def toJson(fieldPath: Option[String]): Json =
    Obj(
      "match" -> Obj(
        Chunk(
          Some("query" -> query.toJson),
          analyzer.map("analyzer" -> _.toJson),
          IntervalRule.useFieldToJson(useField, fieldPath),
          maxGaps.map("max_gaps" -> _.toJson),
          ordered.map("ordered" -> _.toJson),
          filter.map("filter" -> _.toJson(fieldPath))
        ).flatten: _*
      )
    )
}

sealed trait IntervalPrefixRule[-S]
    extends IntervalRule[S]
    with HasAnalyzer[IntervalPrefixRule[S]]
    with HasUseField[IntervalPrefixRule, S]

private[elasticsearch] final case class IntervalPrefix[S](
  prefix: String,
  analyzer: Option[String],
  useField: Option[String]
) extends IntervalPrefixRule[S] { self =>

  def analyzer(value: String): IntervalPrefixRule[S] = self.copy(analyzer = Some(value))

  def useField[S1 <: S: Schema](field: Field[S1, _]): IntervalPrefixRule[S1] =
    self.copy[S1](useField = Some(field.toString))

  def useField(field: String): IntervalPrefixRule[S] = self.copy(useField = Some(field))

  private[elasticsearch] def toJson(fieldPath: Option[String]): Json =
    Obj(
      "prefix" -> Obj(
        Chunk(
          Some("prefix" -> prefix.toJson),
          analyzer.map("analyzer" -> _.toJson),
          IntervalRule.useFieldToJson(useField, fieldPath)
        ).flatten: _*
      )
    )
}

sealed trait IntervalRangeRule[-S, LB <: LowerBound, UB <: UpperBound]
    extends IntervalRule[S]
    with HasAnalyzer[IntervalRangeRule[S, LB, UB]]
    with HasUseField[({ type Q[-S1] = IntervalRangeRule[S1, LB, UB] })#Q, S] {

  /**
   * Sets the greater-than bound for this `range` interval rule. The lower bound can be set only once.
   *
   * @param value
   *   the value for the greater-than bound
   * @return
   *   a new instance of the interval rule with the greater-than bound set.
   */
  def gt(value: String)(implicit ev: LB =:= Unbounded.type): IntervalRangeRule[S, GreaterThan[String], UB]

  /**
   * Sets the greater-than-or-equal-to bound for this `range` interval rule. The lower bound can be set only once.
   *
   * @param value
   *   the value for the greater-than-or-equal-to bound
   * @return
   *   a new instance of the interval rule with the greater-than-or-equal-to bound set.
   */
  def gte(value: String)(implicit ev: LB =:= Unbounded.type): IntervalRangeRule[S, GreaterThanOrEqualTo[String], UB]

  /**
   * Sets the less-than bound for this `range` interval rule. The upper bound can be set only once.
   *
   * @param value
   *   the value for the less-than bound
   * @return
   *   a new instance of the interval rule with the less-than bound set.
   */
  def lt(value: String)(implicit ev: UB =:= Unbounded.type): IntervalRangeRule[S, LB, LessThan[String]]

  /**
   * Sets the less-than-or-equal-to bound for this `range` interval rule. The upper bound can be set only once.
   *
   * @param value
   *   the value for the less-than-or-equal-to bound
   * @return
   *   a new instance of the interval rule with the less-than-or-equal-to bound set.
   */
  def lte(value: String)(implicit ev: UB =:= Unbounded.type): IntervalRangeRule[S, LB, LessThanOrEqualTo[String]]
}

private[elasticsearch] final case class IntervalRange[S, LB <: LowerBound, UB <: UpperBound](
  lower: LB,
  upper: UB,
  analyzer: Option[String],
  useField: Option[String]
) extends IntervalRangeRule[S, LB, UB] { self =>

  def analyzer(value: String): IntervalRangeRule[S, LB, UB] = self.copy(analyzer = Some(value))

  def gt(value: String)(implicit ev: LB =:= Unbounded.type): IntervalRangeRule[S, GreaterThan[String], UB] =
    self.copy(lower = GreaterThan(value))

  def gte(value: String)(implicit ev: LB =:= Unbounded.type): IntervalRangeRule[S, GreaterThanOrEqualTo[String], UB] =
    self.copy(lower = GreaterThanOrEqualTo(value))

  def lt(value: String)(implicit ev: UB =:= Unbounded.type): IntervalRangeRule[S, LB, LessThan[String]] =
    self.copy(upper = LessThan(value))

  def lte(value: String)(implicit ev: UB =:= Unbounded.type): IntervalRangeRule[S, LB, LessThanOrEqualTo[String]] =
    self.copy(upper = LessThanOrEqualTo(value))

  def useField[S1 <: S: Schema](field: Field[S1, _]): IntervalRangeRule[S1, LB, UB] =
    self.copy[S1, LB, UB](useField = Some(field.toString))

  def useField(field: String): IntervalRangeRule[S, LB, UB] = self.copy(useField = Some(field))

  private[elasticsearch] def toJson(fieldPath: Option[String]): Json =
    Obj(
      "range" -> Obj(
        Chunk(
          lower.toJson,
          upper.toJson,
          analyzer.map("analyzer" -> _.toJson),
          IntervalRule.useFieldToJson(useField, fieldPath)
        ).flatten: _*
      )
    )
}

sealed trait IntervalRegexpRule[-S]
    extends IntervalRule[S]
    with HasAnalyzer[IntervalRegexpRule[S]]
    with HasUseField[IntervalRegexpRule, S]

private[elasticsearch] final case class IntervalRegexp[S](
  pattern: String,
  analyzer: Option[String],
  useField: Option[String]
) extends IntervalRegexpRule[S] { self =>

  def analyzer(value: String): IntervalRegexpRule[S] = self.copy(analyzer = Some(value))

  def useField[S1 <: S: Schema](field: Field[S1, _]): IntervalRegexpRule[S1] =
    self.copy[S1](useField = Some(field.toString))

  def useField(field: String): IntervalRegexpRule[S] = self.copy(useField = Some(field))

  private[elasticsearch] def toJson(fieldPath: Option[String]): Json =
    Obj(
      "regexp" -> Obj(
        Chunk(
          Some("pattern" -> pattern.toJson),
          analyzer.map("analyzer" -> _.toJson),
          IntervalRule.useFieldToJson(useField, fieldPath)
        ).flatten: _*
      )
    )
}

sealed trait IntervalWildcardRule[-S]
    extends IntervalRule[S]
    with HasAnalyzer[IntervalWildcardRule[S]]
    with HasUseField[IntervalWildcardRule, S]

private[elasticsearch] final case class IntervalWildcard[S](
  pattern: String,
  analyzer: Option[String],
  useField: Option[String]
) extends IntervalWildcardRule[S] { self =>

  def analyzer(value: String): IntervalWildcardRule[S] = self.copy(analyzer = Some(value))

  def useField[S1 <: S: Schema](field: Field[S1, _]): IntervalWildcardRule[S1] =
    self.copy[S1](useField = Some(field.toString))

  def useField(field: String): IntervalWildcardRule[S] = self.copy(useField = Some(field))

  private[elasticsearch] def toJson(fieldPath: Option[String]): Json =
    Obj(
      "wildcard" -> Obj(
        Chunk(
          Some("pattern" -> pattern.toJson),
          analyzer.map("analyzer" -> _.toJson),
          IntervalRule.useFieldToJson(useField, fieldPath)
        ).flatten: _*
      )
    )
}
