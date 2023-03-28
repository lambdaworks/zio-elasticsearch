package zio.elasticsearch.query.sort

sealed trait SourceType

object SourceType {
  final case object NumberType extends SourceType {
    override def toString: String = "number"
  }

  final case object StringType extends SourceType {
    override def toString: String = "string"
  }
}
