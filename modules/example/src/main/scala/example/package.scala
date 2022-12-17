import zio.elasticsearch.IndexName

package object example {
  val Index: IndexName.Type = IndexName("repositories")
}
