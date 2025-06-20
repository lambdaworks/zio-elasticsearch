package zio.elasticsearch

import zio.NonEmptyChunk
import zio.elasticsearch.query.{
  IntervalAllOf,
  IntervalAnyOf,
  IntervalFilter,
  IntervalFuzzy,
  IntervalMatch,
  IntervalPrefix,
  IntervalRange,
  IntervalRegexp,
  IntervalRule,
  IntervalWildcard,
  Regexp
}
import zio.json.ast.Json

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

    Some(filter).filterNot(_ =>
      List(after, before, containedBy, containing, notContainedBy, notContaining, notOverlapping, overlapping, script)
        .forall(_.isEmpty)
    )
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
