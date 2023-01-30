package example.api

import zio.schema.{DeriveSchema, Schema}

import java.time.LocalDateTime

sealed trait Criteria

object Criteria {
  implicit val schema: Schema[Criteria] = DeriveSchema.gen[Criteria]
}

final case class CompoundCriteria(operator: CompoundOperator, filters: List[Criteria]) extends Criteria

final case class DateCriteria(field: DateFilter, operator: FilterOperator, value: LocalDateTime) extends Criteria

final case class IntCriteria(field: IntFilter, operator: FilterOperator, value: Int) extends Criteria

final case class StringCriteria(field: StringFilter, operator: StringFilterOperator, value: String) extends Criteria

sealed trait CompoundOperator

object CompoundOperator {
  case object And extends CompoundOperator
  case object Or  extends CompoundOperator
}

sealed trait FilterOperator

object FilterOperator {
  case object GreaterThan extends FilterOperator
  case object LessThan    extends FilterOperator
  case object EqualTo     extends FilterOperator
}

sealed trait StringFilterOperator

object StringFilterOperator {
  case object Contains   extends StringFilterOperator
  case object StartsWith extends StringFilterOperator
  case object Pattern    extends StringFilterOperator
}

sealed trait DateFilter

case object LastCommitAt extends DateFilter {
  override def toString: String = "lastCommitAt"
}

sealed trait IntFilter

object IntFilter {
  case object Stars extends IntFilter {
    override def toString: String = "stars"
  }

  case object Forks extends IntFilter {
    override def toString: String = "forks"
  }
}

sealed trait StringFilter

object StringFilter {
  case object Id extends StringFilter {
    override def toString: String = "id"
  }

  case object Organization extends StringFilter {
    override def toString: String = "organization"
  }

  case object Name extends StringFilter {
    override def toString: String = "name"
  }

  case object Url extends StringFilter {
    override def toString: String = "url"
  }

  case object Description extends StringFilter {
    override def toString: String = "description"
  }
}
