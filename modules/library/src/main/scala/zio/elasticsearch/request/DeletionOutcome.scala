package zio.elasticsearch.request

sealed abstract class DeletionOutcome

object DeletionOutcome {
  case object Deleted  extends DeletionOutcome
  case object NotFound extends DeletionOutcome
}
