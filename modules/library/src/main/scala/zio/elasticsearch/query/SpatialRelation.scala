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

import zio.json.ast.Json
import zio.json.ast.Json.Str

sealed trait SpatialRelation {
  def value: String
  def toJson: Json = Str(value)
}

object SpatialRelation {
  case object Intersects extends SpatialRelation { def value: String = "intersects" }
  case object Disjoint   extends SpatialRelation { def value: String = "disjoint"   }
  case object Within     extends SpatialRelation { def value: String = "within"     }
  case object Contains   extends SpatialRelation { def value: String = "contains"   }
}
