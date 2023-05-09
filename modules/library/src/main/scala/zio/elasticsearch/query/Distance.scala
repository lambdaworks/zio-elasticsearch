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

final case class Distance(distanceValue: Double, distanceUnit: DistanceUnit) {
  override def toString: String = s"$distanceValue$distanceUnit"
}

sealed trait DistanceUnit {
  def value: String
  override def toString: String = value
}

object DistanceUnit {
  case object Centimeter   extends DistanceUnit { def value: String = "cm"  }
  case object Feet         extends DistanceUnit { def value: String = "ft"  }
  case object Inch         extends DistanceUnit { def value: String = "in"  }
  case object Kilometers   extends DistanceUnit { def value: String = "km"  }
  case object Mile         extends DistanceUnit { def value: String = "mi"  }
  case object Meter        extends DistanceUnit { def value: String = "m"   }
  case object Milimeter    extends DistanceUnit { def value: String = "mm"  }
  case object NauticalMile extends DistanceUnit { def value: String = "nmi" }
  case object Yard         extends DistanceUnit { def value: String = "yd"  }
}

sealed trait DistanceType {
  def value: String
  override def toString: String = value
}

object DistanceType {
  case object Arc   extends DistanceType { def value: String = "arc"   }
  case object Plane extends DistanceType { def value: String = "plane" }

}

sealed trait ValidationMethod {
  def value: String
  override def toString: String = value
}

object ValidationMethod {
  case object Coerce          extends ValidationMethod { def value: String = "COERCE"           }
  case object IgnoreMalformed extends ValidationMethod { def value: String = "IGNORE_MALFORMED" }
  case object Strict          extends ValidationMethod { def value: String = "STRICT"           }
}
