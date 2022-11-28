package zio.elasticsearch

final case class IndexName(name: String) extends AnyVal {
  override def toString: String = name
}
