package zio.elasticsearch.query

import zio.{Chunk, NonEmptyChunk}
import zio.elasticsearch.ElasticPrimitive.ElasticPrimitiveOps
import zio.json.ast.Json
import zio.json.ast.Json.{Arr, Obj, Str}

sealed trait IntervalRule {
  private[elasticsearch] def toJson: Json
}
sealed trait BoundType
sealed trait Inclusive extends BoundType
sealed trait Exclusive extends BoundType

case object InclusiveBound extends Inclusive
case object ExclusiveBound extends Exclusive

final case class Bound[B <: BoundType](value: String, boundType: B)

private[elasticsearch] final case class IntervalAllOf[S](
  intervals: Chunk[IntervalRule],
  maxGaps: Option[Int],
  ordered: Option[Boolean],
  filter: Option[IntervalFilter[S]]
) extends IntervalRule {
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

  def withFilter(f: IntervalFilter[S]): IntervalAnyOf[S] = copy(filter = Some(f))

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
) extends IntervalRule {
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
) extends IntervalRule { self =>

  def withAnalyzer(a: String)          = copy(analyzer = Some(a))
  def withUseField(f: String)          = copy(useField = Some(f))
  def withMaxGaps(g: Int)              = copy(maxGaps = Some(g))
  def withOrdered(o: Boolean)          = copy(ordered = Some(o))
  def withFilter(f: IntervalFilter[S]) = copy(filter = Some(f))

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

private[elasticsearch] final case class IntervalPrefix(
  prefix: String,
  analyzer: Option[String],
  useField: Option[String]
) extends IntervalRule {

  def withAnalyzer(a: String) = copy(analyzer = Some(a))
  def withUseField(f: String) = copy(useField = Some(f))

  override private[elasticsearch] def toJson: Json =
    Obj(
      "prefix" -> Obj(
        Chunk(
          Some("prefix" -> Str(prefix)),
          analyzer.map(a => "analyzer" -> Str(a)),
          useField.map(u => "use_field" -> Str(u))
        ).flatten: _*
      )
    )
}

final case class IntervalRange[S](
  lower: Option[Bound[_ <: BoundType]] = None,
  upper: Option[Bound[_ <: BoundType]] = None,
  analyzer: Option[String] = None,
  useField: Option[String] = None
) extends IntervalRule {
  private[elasticsearch] def toJson: Json = {
    def boundToJson[B <: BoundType](bound: Bound[B], isLower: Boolean): (String, Json) = {
      val key = (
        isLower,
        bound.boundType match {
          case InclusiveBound => true
          case ExclusiveBound => false
        }
      ) match {
        case (true, true)   => "gte"
        case (true, false)  => "gt"
        case (false, true)  => "lte"
        case (false, false) => "lt"
      }
      key -> Json.Str(bound.value)
    }

    val lowerJson = lower.map(bound => boundToJson(bound, isLower = true))
    val upperJson = upper.map(bound => boundToJson(bound, isLower = false))

    Obj(
      "range" -> Obj(
        Chunk(
          lowerJson,
          upperJson,
          analyzer.map("analyzer" -> Str(_)),
          useField.map("use_field" -> Str(_))
        ).flatten: _*
      )
    )
  }
}

private[elasticsearch] final case class IntervalRegexp[S](
  pattern: Regexp[S],
  analyzer: Option[String],
  useField: Option[String]
) extends IntervalRule {
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
) extends IntervalRule {

  def withAnalyzer(a: String): IntervalWildcard[S] =
    copy(analyzer = Some(a))

  def withUseField(f: String): IntervalWildcard[S] =
    copy(useField = Some(f))

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
    IntervalAllOf(intervals, maxGaps = None, ordered = None, filter = None)

  def intervalAnyOf[S](intervals: NonEmptyChunk[IntervalRule]): IntervalAnyOf[S] =
    IntervalAnyOf(intervals, filter = None)

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
      after,
      before,
      containedBy,
      containing,
      notContainedBy,
      notContaining,
      notOverlapping,
      overlapping,
      script
    )

    val isEmpty = Seq(
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

    if (isEmpty) None else Some(filter)
  }

  def intervalFuzzy[S](term: String): IntervalFuzzy[S] =
    IntervalFuzzy(term, prefixLength = None, transpositions = None, fuzziness = None, analyzer = None, useField = None)

  def intervalMatch[S](query: String): IntervalMatch[S] =
    IntervalMatch(query, analyzer = None, useField = None, maxGaps = None, ordered = None, filter = None)

  def intervalPrefix[S](prefix: String): IntervalPrefix =
    IntervalPrefix(prefix, analyzer = None, useField = None)

  def intervalRange[S, L <: BoundType, U <: BoundType](
    lower: Option[Bound[L]] = None,
    upper: Option[Bound[U]] = None,
    analyzer: Option[String] = None,
    useField: Option[String] = None
  ): IntervalRange[S] =
    IntervalRange(lower, upper, analyzer, useField)

  def intervalRegexp[S](pattern: Regexp[S]): IntervalRegexp[S] =
    IntervalRegexp(pattern, analyzer = None, useField = None)

  def intervalStartsWith[S](pattern: String): IntervalWildcard[S] =
    IntervalWildcard(s"$pattern*", analyzer = None, useField = None)

  def intervalWildcard[S](pattern: String): IntervalWildcard[S] =
    IntervalWildcard(pattern, analyzer = None, useField = None)
}
