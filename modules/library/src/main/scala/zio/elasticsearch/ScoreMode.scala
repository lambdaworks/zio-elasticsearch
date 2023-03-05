package zio.elasticsearch

import zio.elasticsearch.ElasticQuery.NestedQuery
import zio.elasticsearch.ElasticQueryType.Nested

sealed trait ScoreMode

object ScoreMode {

  final case object Avg  extends ScoreMode
  final case object Max  extends ScoreMode
  final case object Min  extends ScoreMode
  final case object None extends ScoreMode
  final case object Sum  extends ScoreMode

  trait WithScoreMode[EQT <: ElasticQueryType] {
    def withScoreMode(query: ElasticQuery[EQT], scoreMode: ScoreMode): ElasticQuery[EQT]
  }

  object WithScoreMode {
    implicit val nestedWithScoreMode: WithScoreMode[Nested] =
      (query: ElasticQuery[Nested], scoreMode: ScoreMode) =>
        query match {
          case q: NestedQuery => q.copy(scoreMode = Some(scoreMode))
        }
  }
}
