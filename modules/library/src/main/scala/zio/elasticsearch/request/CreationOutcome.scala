package zio.elasticsearch.request

sealed abstract class CreationOutcome

object CreationOutcome {
  case object AlreadyExists extends CreationOutcome
  case object Created       extends CreationOutcome
}
