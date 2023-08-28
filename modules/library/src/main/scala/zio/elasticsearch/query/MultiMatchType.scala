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

sealed trait MultiMatchType

object MultiMatchType {
  case object BestFields   extends MultiMatchType { override def toString: String = "best_fields"   }
  case object BoolPrefix   extends MultiMatchType { override def toString: String = "bool_prefix"   }
  case object CrossFields  extends MultiMatchType { override def toString: String = "cross_fields"  }
  case object MostFields   extends MultiMatchType { override def toString: String = "most_fields"   }
  case object Phrase       extends MultiMatchType { override def toString: String = "phrase_fields" }
  case object PhrasePrefix extends MultiMatchType { override def toString: String = "phrase_prefix" }

}
