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

package zio.elasticsearch

import zio.NonEmptyChunk
import zio.elasticsearch.query.{
  IntervalAllOf,
  IntervalAllOfRule,
  IntervalAnyOf,
  IntervalAnyOfRule,
  IntervalFilter,
  IntervalFuzzy,
  IntervalFuzzyRule,
  IntervalMatch,
  IntervalMatchRule,
  IntervalPrefix,
  IntervalPrefixRule,
  IntervalRange,
  IntervalRangeRule,
  IntervalRegexp,
  IntervalRegexpRule,
  IntervalRule,
  IntervalWildcard,
  IntervalWildcardRule
}
import zio.json.ast.Json

object ElasticIntervalRule {

  /**
   * Constructs an instance of [[zio.elasticsearch.query.IntervalRule]] that requires all the given `intervals` to
   * match, in any order unless [[zio.elasticsearch.query.IntervalAllOfRule#orderedOn]] is used.
   *
   * @param intervals
   *   the interval rules that all have to match
   * @tparam S
   *   the document type on which the interval rule is executed
   * @return
   *   an instance of [[zio.elasticsearch.query.IntervalRule]] representing the `all_of` interval rule.
   */
  def intervalAllOf[S](intervals: NonEmptyChunk[IntervalRule]): IntervalAllOfRule[S] =
    IntervalAllOf(intervals = intervals, maxGaps = None, ordered = None, filter = None)

  /**
   * Constructs an instance of [[zio.elasticsearch.query.IntervalRule]] that requires any one of the given `intervals`
   * to match.
   *
   * @param intervals
   *   the interval rules, any one of which has to match
   * @tparam S
   *   the document type on which the interval rule is executed
   * @return
   *   an instance of [[zio.elasticsearch.query.IntervalRule]] representing the `any_of` interval rule.
   */
  def intervalAnyOf[S](intervals: NonEmptyChunk[IntervalRule]): IntervalAnyOfRule[S] =
    IntervalAnyOf(intervals = intervals, filter = None)

  /**
   * Constructs an instance of [[zio.elasticsearch.query.IntervalRule]] that matches terms containing the specified
   * `pattern`.
   *
   * @param pattern
   *   the substring that a matching term must contain
   * @tparam S
   *   the document type on which the interval rule is executed
   * @return
   *   an instance of [[zio.elasticsearch.query.IntervalRule]] representing the `wildcard` interval rule.
   */
  def intervalContains[S](pattern: String): IntervalWildcardRule[S] =
    IntervalWildcard(s"*$pattern*", analyzer = None, useField = None)

  /**
   * Constructs an instance of [[zio.elasticsearch.query.IntervalRule]] that matches terms ending with the specified
   * `pattern`.
   *
   * @param pattern
   *   the suffix that a matching term must end with
   * @tparam S
   *   the document type on which the interval rule is executed
   * @return
   *   an instance of [[zio.elasticsearch.query.IntervalRule]] representing the `wildcard` interval rule.
   */
  def intervalEndsWith[S](pattern: String): IntervalWildcardRule[S] =
    IntervalWildcard(s"*$pattern", analyzer = None, useField = None)

  /**
   * Constructs an instance of [[zio.elasticsearch.query.IntervalFilter]], used to restrict the intervals matched by
   * another [[zio.elasticsearch.query.IntervalRule]] based on their relation to the intervals produced by the specified
   * rules.
   *
   * @param after
   *   restricts matches to intervals that follow an interval from the given rule
   * @param before
   *   restricts matches to intervals that precede an interval from the given rule
   * @param containedBy
   *   restricts matches to intervals contained by an interval from the given rule
   * @param containing
   *   restricts matches to intervals that contain an interval from the given rule
   * @param notContainedBy
   *   restricts matches to intervals that are not contained by an interval from the given rule
   * @param notContaining
   *   restricts matches to intervals that do not contain an interval from the given rule
   * @param notOverlapping
   *   restricts matches to intervals that do not overlap with an interval from the given rule
   * @param overlapping
   *   restricts matches to intervals that overlap with an interval from the given rule
   * @param script
   *   restricts matches using the specified script
   * @tparam S
   *   the document type on which the interval rule is executed
   * @return
   *   an instance of [[zio.elasticsearch.query.IntervalFilter]] representing the interval filter.
   */
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
  ): IntervalFilter[S] =
    IntervalFilter(
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

  /**
   * Constructs an instance of [[zio.elasticsearch.query.IntervalRule]] that matches terms similar to the specified
   * `term`, up to an edit distance defined by the fuzziness.
   *
   * @param term
   *   the term to fuzzy match against
   * @tparam S
   *   the document type on which the interval rule is executed
   * @return
   *   an instance of [[zio.elasticsearch.query.IntervalRule]] representing the `fuzzy` interval rule.
   */
  def intervalFuzzy[S](term: String): IntervalFuzzyRule[S] =
    IntervalFuzzy(
      term = term,
      prefixLength = None,
      transpositions = None,
      fuzziness = None,
      analyzer = None,
      useField = None
    )

  /**
   * Constructs an instance of [[zio.elasticsearch.query.IntervalRule]] that matches analyzed text within specified
   * intervals based on the provided query string.
   *
   * @param query
   *   the text to match in the intervals
   * @tparam S
   *   the document type on which the interval rule is executed
   * @return
   *   an instance of [[zio.elasticsearch.query.IntervalRule]] representing the `match` interval rule.
   */
  def intervalMatch[S](query: String): IntervalMatchRule[S] =
    IntervalMatch(query = query, analyzer = None, useField = None, maxGaps = None, ordered = None, filter = None)

  /**
   * Constructs an instance of [[zio.elasticsearch.query.IntervalRule]] that matches terms starting with the specified
   * `prefix`.
   *
   * @param prefix
   *   the prefix string to match
   * @tparam S
   *   the document type on which the interval rule is executed
   * @return
   *   an instance of [[zio.elasticsearch.query.IntervalRule]] representing the `prefix` interval rule.
   */
  def intervalPrefix[S](prefix: String): IntervalPrefixRule[S] =
    IntervalPrefix(prefix = prefix, analyzer = None, useField = None)

  /**
   * Constructs an unbounded instance of [[zio.elasticsearch.query.IntervalRule]] that matches terms within a range. The
   * bounds can be set using the `gt`, `gte`, `lt` and `lte` methods.
   *
   * @tparam S
   *   the document type on which the interval rule is executed
   * @return
   *   an instance of [[zio.elasticsearch.query.IntervalRule]] representing the `range` interval rule.
   */
  def intervalRange[S]: IntervalRangeRule[S] =
    IntervalRange(lower = None, upper = None, analyzer = None, useField = None)

  /**
   * Constructs an instance of [[zio.elasticsearch.query.IntervalRule]] that matches terms using the specified regular
   * expression `pattern`.
   *
   * @param pattern
   *   the regular expression to match
   * @tparam S
   *   the document type on which the interval rule is executed
   * @return
   *   an instance of [[zio.elasticsearch.query.IntervalRule]] representing the `regexp` interval rule.
   */
  def intervalRegexp[S](pattern: String): IntervalRegexpRule[S] =
    IntervalRegexp(pattern = pattern, analyzer = None, useField = None)

  /**
   * Constructs an instance of [[zio.elasticsearch.query.IntervalRule]] that matches terms starting with the specified
   * `pattern`.
   *
   * @param pattern
   *   the prefix that a matching term must start with
   * @tparam S
   *   the document type on which the interval rule is executed
   * @return
   *   an instance of [[zio.elasticsearch.query.IntervalRule]] representing the `wildcard` interval rule.
   */
  def intervalStartsWith[S](pattern: String): IntervalWildcardRule[S] =
    IntervalWildcard(s"$pattern*", analyzer = None, useField = None)

  /**
   * Constructs an instance of [[zio.elasticsearch.query.IntervalRule]] that matches terms using the specified wildcard
   * `pattern`.
   *
   * @param pattern
   *   the wildcard pattern to match, using `*` to match zero or more characters and `?` to match a single character
   * @tparam S
   *   the document type on which the interval rule is executed
   * @return
   *   an instance of [[zio.elasticsearch.query.IntervalRule]] representing the `wildcard` interval rule.
   */
  def intervalWildcard[S](pattern: String): IntervalWildcardRule[S] =
    IntervalWildcard(pattern = pattern, analyzer = None, useField = None)
}
