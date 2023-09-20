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

  override def toString: String =
    s"$distanceValue$distanceUnit"
}

sealed trait DistanceUnit
object DistanceUnit {
  case object Centimeters extends DistanceUnit {
    override def toString: String =
      "cm"
  }

  case object Feet extends DistanceUnit {
    override def toString: String =
      "ft"
  }

  case object Inches extends DistanceUnit {
    override def toString: String = "in"
  }

  case object Kilometers extends DistanceUnit {
    override def toString: String =
      "km"
  }

  case object Miles extends DistanceUnit {
    override def toString =
      "mi"
  }

  case object Meters extends DistanceUnit {
    override def toString: String =
      "m"
  }

  case object Millimeters extends DistanceUnit {
    override def toString: String =
      "mm"
  }

  case object NauticalMiles extends DistanceUnit {
    override def toString: String =
      "nmi"
  }

  case object Yards extends DistanceUnit {
    override def toString: String =
      "yd"
  }

}

sealed trait DistanceType

object DistanceType {
  case object Arc extends DistanceType {
    override def toString: String =
      "arc"
  }

  case object Plane extends DistanceType {
    override def toString: String =
      "plane"
  }

}

sealed trait ValidationMethod

object ValidationMethod {
  case object Coerce extends ValidationMethod {
    override def toString: String =
      "COERCE"
  }

  case object IgnoreMalformed extends ValidationMethod {
    override def toString: String =
      "IGNORE_MALFORMED"
  }

  case object Strict extends ValidationMethod {
    override def toString: String = "STRICT"
  }
}
