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

sealed trait MultiMatchType {
  def value: String
  override def toString: String = value
}

object MultiMatchType {
  case object BestFields   extends MultiMatchType { def value: String = "best_fields"   }
  case object BoolPrefix   extends MultiMatchType { def value: String = "bool_prefix"   }
  case object CrossFields  extends MultiMatchType { def value: String = "cross_fields"  }
  case object MostFields   extends MultiMatchType { def value: String = "most_fields"   }
  case object Phrase       extends MultiMatchType { def value: String = "phrase_fields" }
  case object PhrasePrefix extends MultiMatchType { def value: String = "phrase_prefix" }
}
