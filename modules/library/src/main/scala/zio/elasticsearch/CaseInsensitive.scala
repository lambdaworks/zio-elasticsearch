package zio.elasticsearch

import zio.elasticsearch.ElasticQuery.TermQuery
import zio.elasticsearch.ElasticQueryType.Term

object CaseInsensitive {

  trait WithCaseInsensitive[EQT <: ElasticQueryType] {
    def withCaseInsensitive(query: ElasticQuery[EQT], value: Boolean): ElasticQuery[EQT]
  }

  object WithCaseInsensitive {
    implicit val termWithCaseInsensitiveString: WithCaseInsensitive[Term[String]] =
      (query: ElasticQuery[Term[String]], value: Boolean) =>
        query match {
          case q: TermQuery[String] => q.copy(caseInsensitive = Some(value))
        }
  }
}
