import zio.elasticsearch.IndexName
import zio.prelude.Newtype.unsafeWrap

package object example {
  final val Index: IndexName     = unsafeWrap(IndexName)("repositories")
  final val organization: String = "zio"
}
