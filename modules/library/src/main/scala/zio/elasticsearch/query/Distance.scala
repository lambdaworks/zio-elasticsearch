package zio.elasticsearch.query

final case class Distance(distanceValue: Double, distanceUnit: DistanceUnit) {
  override def toString = s"$distanceValue$distanceUnit"
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
