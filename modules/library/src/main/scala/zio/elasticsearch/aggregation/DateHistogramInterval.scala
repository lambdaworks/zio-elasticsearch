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

package zio.elasticsearch.aggregation

/**
 * The interval of a [[zio.elasticsearch.aggregation.DateHistogramAggregation]], which is either a
 * [[zio.elasticsearch.aggregation.CalendarInterval]] or a [[zio.elasticsearch.aggregation.FixedInterval]].
 */
sealed trait DateHistogramInterval {
  private[elasticsearch] def paramName: String

  private[elasticsearch] def value: String
}

/**
 * A calendar-aware interval, which takes into account that units such as months have different lengths and that days
 * can be affected by daylight saving time. Only a single unit (e.g. one month) can be specified.
 */
sealed abstract class CalendarInterval(private[elasticsearch] val value: String) extends DateHistogramInterval {
  private[elasticsearch] final def paramName: String = "calendar_interval"
}

object CalendarInterval {

  case object Minute extends CalendarInterval("minute")

  case object Hour extends CalendarInterval("hour")

  case object Day extends CalendarInterval("day")

  case object Week extends CalendarInterval("week")

  case object Month extends CalendarInterval("month")

  case object Quarter extends CalendarInterval("quarter")

  case object Year extends CalendarInterval("year")
}

/**
 * A fixed interval, which is always a multiple of a fixed time unit, regardless of where it falls on the calendar (e.g.
 * `30` days). Fixed intervals do not support units such as months, since their length is not fixed.
 */
sealed trait FixedInterval extends DateHistogramInterval

object FixedInterval {

  private final case class Interval(value: String) extends FixedInterval {
    private[elasticsearch] def paramName: String = "fixed_interval"
  }

  /**
   * Creates a [[zio.elasticsearch.aggregation.FixedInterval]] of the given number of milliseconds.
   */
  def milliseconds(amount: Long): FixedInterval = Interval(s"${amount}ms")

  /**
   * Creates a [[zio.elasticsearch.aggregation.FixedInterval]] of the given number of seconds.
   */
  def seconds(amount: Long): FixedInterval = Interval(s"${amount}s")

  /**
   * Creates a [[zio.elasticsearch.aggregation.FixedInterval]] of the given number of minutes.
   */
  def minutes(amount: Long): FixedInterval = Interval(s"${amount}m")

  /**
   * Creates a [[zio.elasticsearch.aggregation.FixedInterval]] of the given number of hours.
   */
  def hours(amount: Long): FixedInterval = Interval(s"${amount}h")

  /**
   * Creates a [[zio.elasticsearch.aggregation.FixedInterval]] of the given number of days.
   */
  def days(amount: Long): FixedInterval = Interval(s"${amount}d")
}
