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

import java.time.{Instant, LocalDate, LocalDateTime, OffsetDateTime, ZonedDateTime}

/**
 * Evidence that a field of type `A` holds numeric or date values, which is required by type-safe constructors of
 * aggregations that support both (e.g. `min`, `max`, `avg`, `sum` and `stats`).
 *
 * @tparam A
 *   the type of the field
 */
sealed trait NumericOrDate[A]

object NumericOrDate {

  private object Evidence extends NumericOrDate[Any]

  private def evidence[A]: NumericOrDate[A] = Evidence.asInstanceOf[NumericOrDate[A]]

  implicit def numeric[A: Numeric]: NumericOrDate[A] = evidence[A]

  implicit val instant: NumericOrDate[Instant] = evidence[Instant]

  implicit val localDate: NumericOrDate[LocalDate] = evidence[LocalDate]

  implicit val localDateTime: NumericOrDate[LocalDateTime] = evidence[LocalDateTime]

  implicit val offsetDateTime: NumericOrDate[OffsetDateTime] = evidence[OffsetDateTime]

  implicit val zonedDateTime: NumericOrDate[ZonedDateTime] = evidence[ZonedDateTime]
}
