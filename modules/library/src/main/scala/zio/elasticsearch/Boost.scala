package zio.elasticsearch

import zio.elasticsearch.ElasticQuery.{ElasticPrimitive, MatchAllQuery, TermQuery}
import zio.elasticsearch.ElasticQueryType.{MatchAll, Term}

object Boost {

  trait WithBoost[EQT <: ElasticQueryType] {
    def withBoost(query: ElasticQuery[EQT], value: Double): ElasticQuery[EQT]
  }

  object WithBoost {
    implicit val matchAllWithBoost: WithBoost[MatchAll] = (query: ElasticQuery[MatchAll], value: Double) =>
      query match {
        case q: MatchAllQuery => q.copy(boost = Some(value))
      }

    implicit def termWithBoost[A: ElasticPrimitive]: WithBoost[Term[A]] =
      (query: ElasticQuery[Term[A]], value: Double) =>
        query match {
          case q: TermQuery[A] => q.copy(boost = Some(value))
        }
  }
}
