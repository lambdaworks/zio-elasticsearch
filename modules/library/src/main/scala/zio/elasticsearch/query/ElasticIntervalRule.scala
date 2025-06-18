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

import zio.elasticsearch.ElasticPrimitive.ElasticPrimitiveOps
import zio.elasticsearch.Field
import zio.elasticsearch.query.options.{HasAnalyzer, HasUseField}
import zio.json.ast.Json
import zio.json.ast.Json.{Arr, Obj, Str}
import zio.{Chunk, NonEmptyChunk}

sealed trait BoundType
sealed trait Inclusive extends BoundType
sealed trait Exclusive extends BoundType

case object InclusiveBound extends Inclusive
case object ExclusiveBound extends Exclusive

private[elasticsearch] final case class Bound[B <: BoundType](value: String, boundType: B)

sealed trait IntervalRule {
  private[elasticsearch] def toJson: Json
}

private[elasticsearch] final case class GreaterThanInterval(value: String) extends IntervalRule {
  private[elasticsearch] def toJson: Json = Str(value)
}

private[elasticsearch] final case class GreaterThanOrEqualToInterval(value: String) extends IntervalRule {
  private[elasticsearch] def toJson: Json = Str(value)
}

private[elasticsearch] final case class LessThanInterval(value: String) extends IntervalRule {
  private[elasticsearch] def toJson: Json = Str(value)
}

private[elasticsearch] final case class LessThanOrEqualToInterval(value: String) extends IntervalRule {
  private[elasticsearch] def toJson: Json = Str(value)
}

private[elasticsearch] final case class IntervalAllOf[S](
  intervals: Chunk[IntervalRule],
  maxGaps: Option[Int],
  ordered: Option[Boolean],
  filter: Option[IntervalFilter[S]]
) extends IntervalRule {

  def filter(f: IntervalFilter[S]): IntervalAllOf[S] = copy(filter = Some(f))

  def maxGaps(g: Int): IntervalAllOf[S] = copy(maxGaps = Some(g))

  def orderedOn: IntervalAllOf[S] = copy(ordered = Some(true))

  def orderedOff: IntervalAllOf[S] = copy(ordered = Some(false))

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

private[elasticsearch] final case class IntervalAnyOf[S](
  intervals: Chunk[IntervalRule],
  filter: Option[IntervalFilter[S]]
) extends IntervalRule { self =>

  def filter(f: IntervalFilter[S]): IntervalAnyOf[S] = copy(filter = Some(f))

  override private[elasticsearch] def toJson: Json =
    Obj(
      "any_of" -> Obj(
        Chunk(
          Some("intervals" -> Arr(intervals.map(_.toJson): _*)),
          filter.map("filter" -> _.toJson)
        ).flatten: _*
      )
    )
}

private[elasticsearch] final case class IntervalFilter[S](
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

private[elasticsearch] final case class IntervalFuzzy[S](
  term: String,
  prefixLength: Option[Int],
  transpositions: Option[Boolean],
  fuzziness: Option[String],
  analyzer: Option[String],
  useField: Option[String]
) extends IntervalRule
    with HasAnalyzer[IntervalFuzzy[S]]
    with HasUseField[IntervalFuzzy[S]] {

  def analyzer(value: String): IntervalFuzzy[S] = copy(analyzer = Some(value))

  def useField(field: Field[_, _]): IntervalFuzzy[S] = copy(useField = Some(field.toString))

  def useField(field: String): IntervalFuzzy[S] = copy(useField = Some(Field(None, field).toString))

  def prefixLength(length: Int): IntervalFuzzy[S] = copy(prefixLength = Some(length))

  def transpositionsEnabled: IntervalFuzzy[S] = copy(transpositions = Some(true))

  def transpositionsDisabled: IntervalFuzzy[S] = copy(transpositions = Some(false))

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

private[elasticsearch] final case class IntervalMatch[S](
  query: String,
  analyzer: Option[String],
  useField: Option[String],
  maxGaps: Option[Int],
  ordered: Option[Boolean],
  filter: Option[IntervalFilter[S]]
) extends IntervalRule
    with HasAnalyzer[IntervalMatch[S]]
    with HasUseField[IntervalMatch[S]] { self =>

  def analyzer(value: String): IntervalMatch[S] = copy(analyzer = Some(value))

  def filter(f: IntervalFilter[S]): IntervalMatch[S] = copy(filter = Some(f))

  def maxGaps(g: Int): IntervalMatch[S] = copy(maxGaps = Some(g))

  def orderedOn: IntervalMatch[S] = copy(ordered = Some(true))

  def orderedOff: IntervalMatch[S] = copy(ordered = Some(false))

  def useField(field: Field[_, _]): IntervalMatch[S] = copy(useField = Some(field.toString))

  def useField(field: String): IntervalMatch[S] = copy(useField = Some(Field(None, field).toString))

  override private[elasticsearch] def toJson: Json =
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

private[elasticsearch] final case class IntervalPrefix[S](
  prefix: String,
  analyzer: Option[String],
  useField: Option[String]
) extends IntervalRule
    with HasAnalyzer[IntervalPrefix[S]]
    with HasUseField[IntervalPrefix[S]] {

  def analyzer(value: String): IntervalPrefix[S] = copy(analyzer = Some(value))

  def useField(field: Field[_, _]): IntervalPrefix[S] = copy(useField = Some(field.toString))

  def useField(field: String): IntervalPrefix[S] = copy(useField = Some(Field(None, field).toString))

  override private[elasticsearch] def toJson: Json =
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

private[elasticsearch] final case class IntervalRange[S](
  lower: Option[IntervalRule] = None,
  upper: Option[IntervalRule] = None,
  analyzer: Option[String] = None,
  useField: Option[String] = None
) extends IntervalRule
    with HasAnalyzer[IntervalRange[S]]
    with HasUseField[IntervalRange[S]] {

  def analyzer(value: String): IntervalRange[S] = copy(analyzer = Some(value))

  def lower[B <: BoundType](b: Bound[B]): IntervalRange[S] = copy(lower = Some(GreaterThanInterval(b.value)))

  def upper[B <: BoundType](b: Bound[B]): IntervalRange[S] = copy(upper = Some(LessThanInterval(b.value)))

  def useField(field: Field[_, _]): IntervalRange[S] = copy(useField = Some(field.toString))

  def useField(field: String): IntervalRange[S] = copy(useField = Some(Field(None, field).toString))

  def gte[B <: BoundType](value: String): IntervalRange[S] =
    copy(lower = Some(GreaterThanOrEqualToInterval(value)))

  def gt[B <: BoundType](value: String): IntervalRange[S] =
    copy(lower = Some(GreaterThanInterval(value)))

  def lte[B <: BoundType](value: String): IntervalRange[S] =
    copy(upper = Some(LessThanOrEqualToInterval(value)))

  def lt[B <: BoundType](value: String): IntervalRange[S] =
    copy(upper = Some(LessThanInterval(value)))

  private[elasticsearch] def toJson: Json =
    Obj(
      "range" -> Obj(
        Chunk(
          lower.map("gte" -> _.toJson),
          upper.map("lte" -> _.toJson),
          analyzer.map("analyzer" -> Str(_)),
          useField.map("use_field" -> _.toJson)
        ).flatten: _*
      )
    )
}

private[elasticsearch] final case class IntervalRegexp[S](
  pattern: Regexp[S],
  analyzer: Option[String],
  useField: Option[String]
) extends IntervalRule
    with HasAnalyzer[IntervalRegexp[S]]
    with HasUseField[IntervalRegexp[S]] {

  def analyzer(value: String): IntervalRegexp[S] = copy(analyzer = Some(value))

  def useField(field: Field[_, _]): IntervalRegexp[S] = copy(useField = Some(field.toString))

  def useField(field: String): IntervalRegexp[S] = copy(useField = Some(Field(None, field).toString))

  private[elasticsearch] def toJson: Json =
    Obj(
      "regexp" -> Obj(
        Chunk(
          Some("pattern" -> pattern.toJson(None)),
          analyzer.map("analyzer" -> _.toJson),
          useField.map("use_field" -> _.toJson)
        ).flatten: _*
      )
    )
}

private[elasticsearch] final case class IntervalWildcard[S](
  pattern: String,
  analyzer: Option[String],
  useField: Option[String]
) extends IntervalRule
    with HasAnalyzer[IntervalWildcard[S]]
    with HasUseField[IntervalWildcard[S]] {

  def analyzer(value: String): IntervalWildcard[S] = copy(analyzer = Some(value))

  def useField(field: Field[_, _]): IntervalWildcard[S] = copy(useField = Some(field.toString))

  def useField(field: String): IntervalWildcard[S] = copy(useField = Some(Field(None, field).toString))

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

object ElasticIntervalQuery {

  def intervalAllOf[S](intervals: NonEmptyChunk[IntervalRule]): IntervalAllOf[S] =
    IntervalAllOf(intervals = intervals, maxGaps = None, ordered = None, filter = None)

  def intervalAnyOf[S](intervals: NonEmptyChunk[IntervalRule]): IntervalAnyOf[S] =
    IntervalAnyOf(intervals = intervals, filter = None)

  def intervalContains[S](pattern: String): IntervalWildcard[S] =
    IntervalWildcard(s"*$pattern*", analyzer = None, useField = None)

  def intervalEndsWith[S](pattern: String): IntervalWildcard[S] =
    IntervalWildcard(s"*$pattern", analyzer = None, useField = None)

  def intervalFilter[S](
    after: Option[IntervalRule] = None,
    before: Option[IntervalRule] = None,
    containedBy: Option[IntervalRule] = None,
    containing: Option[IntervalRule] = None,
    notContainedBy: Option[IntervalRule] = None,
    notContaining: Option[IntervalRule] = None,
    notOverlapping: Option[IntervalRule] = None,
    overlapping: Option[IntervalRule] = None,
    script: Option[Json] = None
  ): Option[IntervalFilter[S]] = {

    val filter: IntervalFilter[S] = IntervalFilter(
      after = after,
      before = before,
      containedBy = containedBy,
      containing = containing,
      notContainedBy = notContainedBy,
      notContaining = notContaining,
      notOverlapping = notOverlapping,
      overlapping = overlapping,
      script = script
    )

    if (
      List(
        after,
        before,
        containedBy,
        containing,
        notContainedBy,
        notContaining,
        notOverlapping,
        overlapping,
        script
      ).forall(_.isEmpty)
    ) None
    else Some(filter)

  }

  def intervalFuzzy[S](term: String): IntervalFuzzy[S] =
    IntervalFuzzy(
      term = term,
      prefixLength = None,
      transpositions = None,
      fuzziness = None,
      analyzer = None,
      useField = None
    )

  def intervalMatch[S](query: String): IntervalMatch[S] =
    IntervalMatch(query = query, analyzer = None, useField = None, maxGaps = None, ordered = None, filter = None)

  def intervalPrefix[S](prefix: String): IntervalPrefix[S] =
    IntervalPrefix(prefix = prefix, analyzer = None, useField = None)

  def intervalRange[S](
    lower: Option[IntervalRule] = None,
    upper: Option[IntervalRule] = None,
    analyzer: Option[String] = None,
    useField: Option[String] = None
  ): IntervalRange[S] =
    IntervalRange(lower = lower, upper = upper, analyzer = analyzer, useField = useField)

  def intervalRegexp[S](pattern: Regexp[S]): IntervalRegexp[S] =
    IntervalRegexp(pattern = pattern, analyzer = None, useField = None)

  def intervalStartsWith[S](pattern: String): IntervalWildcard[S] =
    IntervalWildcard(s"$pattern*", analyzer = None, useField = None)

  def intervalWildcard[S](pattern: String): IntervalWildcard[S] =
    IntervalWildcard(pattern = pattern, analyzer = None, useField = None)
}
