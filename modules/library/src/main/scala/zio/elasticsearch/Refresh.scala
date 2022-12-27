package zio.elasticsearch

import zio.elasticsearch.ElasticRequest.{CreateOrUpdateRequest, CreateRequest, DeleteByIdRequest, Map}
import zio.elasticsearch.ElasticRequestType.{CreateType, DeleteById, Upsert}

object Refresh {

  trait WithRefresh[ERT <: ElasticRequestType] {
    def withRefresh[A](request: ElasticRequest[A, ERT], value: Boolean): ElasticRequest[A, ERT]
  }

  object WithRefresh {
    implicit val createWithRefresh: WithRefresh[CreateType] = new WithRefresh[CreateType] {
      override def withRefresh[A](
        request: ElasticRequest[A, CreateType],
        value: Boolean
      ): ElasticRequest[A, CreateType] =
        request match {
          case Map(r, mapper)   => Map(withRefresh(r, value), mapper)
          case r: CreateRequest => r.copy(refresh = value)
        }
    }
    implicit val upsertWithRefresh: WithRefresh[Upsert] = new WithRefresh[Upsert] {
      override def withRefresh[A](
        request: ElasticRequest[A, Upsert],
        value: Boolean
      ): ElasticRequest[A, Upsert] =
        request match {
          case Map(r, mapper)           => Map(withRefresh(r, value), mapper)
          case r: CreateOrUpdateRequest => r.copy(refresh = value)
        }
    }

    implicit val deleteByIdWithRefresh: WithRefresh[DeleteById] = new WithRefresh[DeleteById] {
      override def withRefresh[A](
        request: ElasticRequest[A, DeleteById],
        value: Boolean
      ): ElasticRequest[A, DeleteById] =
        request match {
          case Map(r, mapper)       => Map(withRefresh(r, value), mapper)
          case r: DeleteByIdRequest => r.copy(refresh = value)
        }
    }
  }
}
