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

import zio.NonEmptyChunk
import zio.json.ast.Json

/**
 * The `minimum_should_match` value refers to the minimum number of optional clauses (e.g., `should` clauses of a
 * [[zio.elasticsearch.query.BoolQuery]] or terms of a full-text query) that a document must match to be returned.
 *
 * @see
 *   [[https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-minimum-should-match.html]]
 */
sealed trait MinimumShouldMatch { self =>

  private[elasticsearch] def toJson: Json =
    self match {
      case MinimumShouldMatch.Count(value) => Json.Num(value)
      case _                               => Json.Str(self.toString)
    }
}

object MinimumShouldMatch {

  /**
   * A `minimum_should_match` value that is either a fixed number or a percentage of optional clauses.
   */
  sealed trait Simple extends MinimumShouldMatch

  /**
   * A fixed number of optional clauses. A positive value is the number of optional clauses that must match, regardless
   * of the total number of optional clauses. A negative value is the number of optional clauses that may be missing,
   * i.e., the total number of optional clauses minus this number must match.
   *
   * @param value
   *   the number of optional clauses that must match (if positive) or may be missing (if negative)
   */
  final case class Count(value: Int) extends Simple {
    override def toString: String = value.toString
  }

  /**
   * A percentage of the total number of optional clauses. A positive value is the percentage of optional clauses that
   * must match, while a negative value is the percentage of optional clauses that may be missing. The computed number
   * is rounded down.
   *
   * @param value
   *   the percentage of optional clauses that must match (if positive) or may be missing (if negative)
   */
  final case class Percentage(value: Int) extends Simple {
    override def toString: String = s"$value%"
  }

  /**
   * A conditional specification used within a [[zio.elasticsearch.query.MinimumShouldMatch.Combination]]. If the total
   * number of optional clauses is less than or equal to `the threshold`, all of them are required. Otherwise, the given
   * `value` applies.
   *
   * @param threshold
   *   the number of optional clauses up to which all of them are required
   * @param value
   *   the [[zio.elasticsearch.query.MinimumShouldMatch.Simple]] value that applies when the number of optional clauses
   *   is greater than `a threshold`
   */
  final case class Condition(threshold: Int, value: Simple) {
    override def toString: String = s"$threshold<$value"
  }

  /**
   * One or more conditional specifications. When multiple conditions are given, each one is only valid for numbers of
   * optional clauses greater than its `threshold`, so the conditions should be given in ascending order of their
   * `threshold` values.
   *
   * Elasticsearch expects conditions with distinct `threshold` values; this type does not validate this.
   *
   * @param conditions
   *   the non-empty chunk of [[zio.elasticsearch.query.MinimumShouldMatch.Condition]]s
   */
  final case class Combination(conditions: NonEmptyChunk[Condition]) extends MinimumShouldMatch { self =>

    /**
     * Adds a [[zio.elasticsearch.query.MinimumShouldMatch.Condition]] to the end of this
     * [[zio.elasticsearch.query.MinimumShouldMatch.Combination]]. Since the conditions should be in ascending order of
     * their `threshold` values, the given condition should have a greater `threshold` than all existing ones.
     *
     * @param condition
     *   the [[zio.elasticsearch.query.MinimumShouldMatch.Condition]] to be added
     * @return
     *   a new instance of [[zio.elasticsearch.query.MinimumShouldMatch.Combination]] with the given condition added.
     */
    def addCondition(condition: Condition): Combination =
      self.copy(conditions = conditions :+ condition)

    override def toString: String = conditions.mkString(" ")
  }

  object Combination {

    /**
     * Constructs a [[zio.elasticsearch.query.MinimumShouldMatch.Combination]] from one or more conditions.
     *
     * @param condition
     *   the first [[zio.elasticsearch.query.MinimumShouldMatch.Condition]]
     * @param conditions
     *   the rest of the [[zio.elasticsearch.query.MinimumShouldMatch.Condition]]s
     * @return
     *   an instance of [[zio.elasticsearch.query.MinimumShouldMatch.Combination]] containing all the given conditions.
     */
    def apply(condition: Condition, conditions: Condition*): Combination =
      Combination(NonEmptyChunk(condition, conditions: _*))
  }
}
