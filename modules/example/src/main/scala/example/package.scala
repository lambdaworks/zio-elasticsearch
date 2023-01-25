import zio.elasticsearch.IndexName

package object example {
  final val Index: IndexName     = IndexName("repositories")
  final val organization: String = "zio"
}
