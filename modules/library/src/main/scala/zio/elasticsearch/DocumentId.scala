package zio.elasticsearch

final case class DocumentId(value: String) extends AnyVal {
  override def toString: String = value
}
