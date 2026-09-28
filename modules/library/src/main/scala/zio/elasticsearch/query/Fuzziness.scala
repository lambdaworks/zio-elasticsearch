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

/**
 * The `fuzziness` value refers to the maximum edit distance allowed when matching terms in a `fuzzy` query or `fuzzy`
 * interval rule.
 */
sealed trait Fuzziness

object Fuzziness {

  /**
   * Chooses an edit distance automatically based on the length of the term, using Elasticsearch's default length
   * thresholds.
   */
  case object Auto extends Fuzziness {
    override def toString: String = "AUTO"
  }

  /**
   * Chooses an edit distance automatically based on the length of the term, using the given length thresholds.
   *
   * @param lowLength
   *   terms shorter than this length must match exactly
   * @param highLength
   *   terms longer than this length are allowed an edit distance of 2; terms with a length between `lowLength` and
   *   `highLength` are allowed an edit distance of 1
   */
  final case class AutoLength(lowLength: Int, highLength: Int) extends Fuzziness {
    override def toString: String = s"AUTO:$lowLength,$highLength"
  }

  /**
   * Uses a fixed maximum edit distance. Elasticsearch only allows `0`, `1` or `2`.
   */
  sealed trait EditDistance extends Fuzziness

  object EditDistance {

    /** Requires an exact match. */
    case object Zero extends EditDistance {
      override def toString: String = "0"
    }

    /** Allows a maximum edit distance of 1. */
    case object One extends EditDistance {
      override def toString: String = "1"
    }

    /** Allows a maximum edit distance of 2. */
    case object Two extends EditDistance {
      override def toString: String = "2"
    }
  }
}
