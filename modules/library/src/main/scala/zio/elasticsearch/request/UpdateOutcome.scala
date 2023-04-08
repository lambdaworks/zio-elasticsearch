package zio.elasticsearch.request

sealed abstract class UpdateOutcome

object UpdateOutcome {
  case object Created extends UpdateOutcome
  case object Updated extends UpdateOutcome
}
