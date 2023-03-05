package zio.elasticsearch

import zio.elasticsearch.ElasticQuery.NestedQuery
import zio.elasticsearch.ElasticQueryType.Nested

object IgnoreUnmapped {

  trait WithIgnoreUnmapped[EQT <: ElasticQueryType] {
    def withIgnoreUnmapped(query: ElasticQuery[EQT], value: Boolean): ElasticQuery[EQT]
  }

  object WithIgnoreUnmapped {
    implicit val nestedWithIgnoreUnmapped: WithIgnoreUnmapped[Nested] =
      (query: ElasticQuery[Nested], value: Boolean) =>
        query match {
          case q: NestedQuery => q.copy(ignoreUnmapped = Some(value))
        }
  }
}
