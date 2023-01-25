package example.external.github.dto

import zio.schema.{DeriveSchema, Schema}

import java.time.LocalDateTime

sealed trait QueryDto

object QueryDto {
  implicit val schema: Schema[QueryDto] = DeriveSchema.gen[QueryDto]
}

case class SimpleIntQueryDto(field: FieldTypeInt, operator: SimpleOperator, value: Int) extends QueryDto

case class SimpleDateQueryDto(field: FieldTypeDate, operator: SimpleOperator, value: LocalDateTime) extends QueryDto

final case class CompoundQueryDto(
  operator: CompoundOperator,
  operands: List[QueryDto]
) extends QueryDto

sealed trait SimpleOperator

object SimpleOperator {
  case object GreaterThan extends SimpleOperator
  case object LessThan    extends SimpleOperator
}

sealed trait CompoundOperator

object CompoundOperator {
  case object And extends CompoundOperator
  case object Or  extends CompoundOperator
}

sealed trait FieldTypeInt

object FieldTypeInt {
  case object Stars extends FieldTypeInt {
    override def toString: String = "stars"
  }
  case object Forks extends FieldTypeInt {
    override def toString: String = "forks"
  }
}

sealed trait FieldTypeDate

case object LastCommitAt extends FieldTypeDate {
  override def toString: String = "lastCommitAt"
}
