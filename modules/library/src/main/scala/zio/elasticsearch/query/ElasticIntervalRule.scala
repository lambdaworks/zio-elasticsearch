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
import zio.elasticsearch.query.options.{HasAnalyzer, HasUseField}
import zio.json.ast.Json
import zio.json.ast.Json.{Arr, Obj, Str}
import zio.schema.Schema

sealed trait IntervalRule {
  private[elasticsearch] def toJson: Json
}

sealed trait IntervalAllOfRule[S] extends IntervalRule {

  /**
   * Sets the `filter` parameter for this `all_of` interval rule, restricting matches to those that also satisfy the
   * given [[zio.elasticsearch.query.IntervalFilter]].
   *
   * @param f
   *   the interval filter to apply
   * @return
   *   a new instance of the interval rule with the `filter` value set.
   */
  def filter(f: IntervalFilter[S]): IntervalAllOfRule[S]

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
  intervals: Chunk[IntervalRule],
  maxGaps: Option[Int],
  ordered: Option[Boolean],
  filter: Option[IntervalFilter[S]]
) extends IntervalAllOfRule[S] { self =>

  def filter(f: IntervalFilter[S]): IntervalAllOfRule[S] = self.copy(filter = Some(f))

  def maxGaps(g: Int): IntervalAllOfRule[S] = self.copy(maxGaps = Some(g))

  def orderedOn: IntervalAllOfRule[S] = self.copy(ordered = Some(true))

  private[elasticsearch] def toJson: Json =
    Obj(
      "all_of" -> Obj(
        Chunk(
          Some("intervals" -> Arr(intervals.map(_.toJson): _*)),
          maxGaps.map("max_gaps" -> _.toJson),
          ordered.map("ordered" -> _.toJson),
          filter.map("filter" -> _.toJson)
        ).flatten: _*
      )
    )
}

sealed trait IntervalAnyOfRule[S] extends IntervalRule {

  /**
   * Sets the `filter` parameter for this `any_of` interval rule, restricting matches to those that also satisfy the
   * given [[zio.elasticsearch.query.IntervalFilter]].
   *
   * @param f
   *   the interval filter to apply
   * @return
   *   a new instance of the interval rule with the `filter` value set.
   */
  def filter(f: IntervalFilter[S]): IntervalAnyOfRule[S]
}

private[elasticsearch] final case class IntervalAnyOf[S](
  intervals: Chunk[IntervalRule],
  filter: Option[IntervalFilter[S]]
) extends IntervalAnyOfRule[S] { self =>

  def filter(f: IntervalFilter[S]): IntervalAnyOfRule[S] = self.copy(filter = Some(f))

  private[elasticsearch] def toJson: Json =
    Obj(
      "any_of" -> Obj(
        Chunk(
          Some("intervals" -> Arr(intervals.map(_.toJson): _*)),
          filter.map("filter" -> _.toJson)
        ).flatten: _*
      )
    )
}

final case class IntervalFilter[S](
  after: Option[IntervalRule] = None,
  before: Option[IntervalRule] = None,
  containedBy: Option[IntervalRule] = None,
  containing: Option[IntervalRule] = None,
  notContainedBy: Option[IntervalRule] = None,
  notContaining: Option[IntervalRule] = None,
  notOverlapping: Option[IntervalRule] = None,
  overlapping: Option[IntervalRule] = None,
  script: Option[Json] = None
) {
  private[elasticsearch] def toJson: Json =
    Obj(
      Chunk(
        after.map("after" -> _.toJson),
        before.map("before" -> _.toJson),
        containedBy.map("contained_by" -> _.toJson),
        containing.map("containing" -> _.toJson),
        notContainedBy.map("not_contained_by" -> _.toJson),
        notContaining.map("not_containing" -> _.toJson),
        notOverlapping.map("not_overlapping" -> _.toJson),
        overlapping.map("overlapping" -> _.toJson),
        script.map("script" -> _)
      ).flatten: _*
    )
}

sealed trait IntervalFuzzyRule[S]
    extends IntervalRule
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

  private[elasticsearch] def toJson: Json =
    Obj(
      "fuzzy" -> Obj(
        Chunk(
          Some("term" -> term.toJson),
          prefixLength.map("prefix_length" -> _.toJson),
          transpositions.map("transpositions" -> _.toJson),
          fuzziness.map("fuzziness" -> _.toJson),
          analyzer.map("analyzer" -> _.toJson),
          useField.map("use_field" -> _.toJson)
        ).flatten: _*
      )
    )
}

sealed trait IntervalMatchRule[S]
    extends IntervalRule
    with HasAnalyzer[IntervalMatchRule[S]]
    with HasUseField[IntervalMatchRule, S] {

  /**
   * Sets the `filter` parameter for this `match` interval rule, restricting matches to those that also satisfy the
   * given [[zio.elasticsearch.query.IntervalFilter]].
   *
   * @param f
   *   the interval filter to apply
   * @return
   *   a new instance of the interval rule with the `filter` value set.
   */
  def filter(f: IntervalFilter[S]): IntervalMatchRule[S]

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

  def filter(f: IntervalFilter[S]): IntervalMatchRule[S] = self.copy(filter = Some(f))

  def maxGaps(g: Int): IntervalMatchRule[S] = self.copy(maxGaps = Some(g))

  def orderedOn: IntervalMatchRule[S] = self.copy(ordered = Some(true))

  def useField[S1 <: S: Schema](field: Field[S1, _]): IntervalMatchRule[S1] =
    self.copy[S1](useField = Some(field.toString), filter = filter.map(_.copy[S1]()))

  def useField(field: String): IntervalMatchRule[S] = self.copy(useField = Some(field))

  private[elasticsearch] def toJson: Json =
    Obj(
      "match" -> Obj(
        Chunk(
          Some("query" -> Str(query)),
          analyzer.map("analyzer" -> _.toJson),
          useField.map("use_field" -> _.toJson),
          maxGaps.map("max_gaps" -> _.toJson),
          ordered.map("ordered" -> _.toJson),
          filter.map("filter" -> _.toJson)
        ).flatten: _*
      )
    )
}

sealed trait IntervalPrefixRule[S]
    extends IntervalRule
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

  private[elasticsearch] def toJson: Json =
    Obj(
      "prefix" -> Obj(
        Chunk(
          Some("prefix" -> Str(prefix)),
          analyzer.map(a => "analyzer" -> Str(a)),
          useField.map("use_field" -> _.toJson)
        ).flatten: _*
      )
    )
}

sealed trait IntervalRangeRule[S]
    extends IntervalRule
    with HasAnalyzer[IntervalRangeRule[S]]
    with HasUseField[IntervalRangeRule, S] {

  /**
   * Sets the greater-than bound for this `range` interval rule.
   *
   * @param value
   *   the value for the greater-than bound
   * @return
   *   a new instance of the interval rule with the greater-than bound set.
   */
  def gt(value: String): IntervalRangeRule[S]

  /**
   * Sets the greater-than-or-equal-to bound for this `range` interval rule.
   *
   * @param value
   *   the value for the greater-than-or-equal-to bound
   * @return
   *   a new instance of the interval rule with the greater-than-or-equal-to bound set.
   */
  def gte(value: String): IntervalRangeRule[S]

  /**
   * Sets the less-than bound for this `range` interval rule.
   *
   * @param value
   *   the value for the less-than bound
   * @return
   *   a new instance of the interval rule with the less-than bound set.
   */
  def lt(value: String): IntervalRangeRule[S]

  /**
   * Sets the less-than-or-equal-to bound for this `range` interval rule.
   *
   * @param value
   *   the value for the less-than-or-equal-to bound
   * @return
   *   a new instance of the interval rule with the less-than-or-equal-to bound set.
   */
  def lte(value: String): IntervalRangeRule[S]
}

private[elasticsearch] final case class IntervalRange[S](
  lower: Option[IntervalRangeBound],
  upper: Option[IntervalRangeBound],
  analyzer: Option[String],
  useField: Option[String]
) extends IntervalRangeRule[S] { self =>

  def analyzer(value: String): IntervalRangeRule[S] = self.copy(analyzer = Some(value))

  def gt(value: String): IntervalRangeRule[S] = self.copy(lower = Some(GreaterThanInterval(value)))

  def gte(value: String): IntervalRangeRule[S] = self.copy(lower = Some(GreaterThanOrEqualToInterval(value)))

  def lt(value: String): IntervalRangeRule[S] = self.copy(upper = Some(LessThanInterval(value)))

  def lte(value: String): IntervalRangeRule[S] = self.copy(upper = Some(LessThanOrEqualToInterval(value)))

  def useField[S1 <: S: Schema](field: Field[S1, _]): IntervalRangeRule[S1] =
    self.copy[S1](useField = Some(field.toString))

  def useField(field: String): IntervalRangeRule[S] = self.copy(useField = Some(field))

  private[elasticsearch] def toJson: Json =
    Obj(
      "range" -> Obj(
        Chunk(
          lower.map(bound => bound.key -> bound.toJson),
          upper.map(bound => bound.key -> bound.toJson),
          analyzer.map("analyzer" -> Str(_)),
          useField.map("use_field" -> _.toJson)
        ).flatten: _*
      )
    )
}

private[elasticsearch] sealed trait IntervalRangeBound extends IntervalRule {
  def key: String
  def value: String

  private[elasticsearch] def toJson: Json = Str(value)
}

private[elasticsearch] final case class GreaterThanInterval(value: String) extends IntervalRangeBound {
  val key: String = "gt"
}

private[elasticsearch] final case class GreaterThanOrEqualToInterval(value: String) extends IntervalRangeBound {
  val key: String = "gte"
}

private[elasticsearch] final case class LessThanInterval(value: String) extends IntervalRangeBound {
  val key: String = "lt"
}

private[elasticsearch] final case class LessThanOrEqualToInterval(value: String) extends IntervalRangeBound {
  val key: String = "lte"
}

sealed trait IntervalRegexpRule[S]
    extends IntervalRule
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

  private[elasticsearch] def toJson: Json =
    Obj(
      "regexp" -> Obj(
        Chunk(
          Some("pattern" -> pattern.toJson),
          analyzer.map("analyzer" -> _.toJson),
          useField.map("use_field" -> _.toJson)
        ).flatten: _*
      )
    )
}

sealed trait IntervalWildcardRule[S]
    extends IntervalRule
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

  private[elasticsearch] def toJson: Json =
    Obj(
      "wildcard" -> Obj(
        Chunk(
          Some("pattern" -> pattern.toJson),
          analyzer.map("analyzer" -> _.toJson),
          useField.map("use_field" -> _.toJson)
        ).flatten: _*
      )
    )
}
