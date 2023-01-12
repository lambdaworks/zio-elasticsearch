package zio.elasticsearch

import zio.elasticsearch.ElasticRequest.{
  CreateOrUpdateRequest,
  CreateRequest,
  CreateWithIdRequest,
  DeleteByIdRequest,
  DeleteByQueryRequest,
  Map
}
import zio.elasticsearch.ElasticRequestType.{Create, CreateWithId, DeleteById, DeleteByQuery, Upsert}

object Refresh {

  trait WithRefresh[ERT <: ElasticRequestType] {
    def withRefresh[A](request: ElasticRequest[A, ERT], value: Boolean): ElasticRequest[A, ERT]
  }

  object WithRefresh {
    implicit val createWithRefresh: WithRefresh[Create] = new WithRefresh[Create] {
      def withRefresh[A](request: ElasticRequest[A, Create], value: Boolean): ElasticRequest[A, Create] =
        request match {
          case Map(r, mapper)   => Map(withRefresh(r, value), mapper)
          case r: CreateRequest => r.copy(refresh = value)
        }
    }

    implicit val createWithIdWithRefresh: WithRefresh[CreateWithId] = new WithRefresh[CreateWithId] {
      def withRefresh[A](request: ElasticRequest[A, CreateWithId], value: Boolean): ElasticRequest[A, CreateWithId] =
        request match {
          case Map(r, mapper)         => Map(withRefresh(r, value), mapper)
          case r: CreateWithIdRequest => r.copy(refresh = value)
        }
    }

    implicit val deleteByIdWithRefresh: WithRefresh[DeleteById] = new WithRefresh[DeleteById] {
      def withRefresh[A](request: ElasticRequest[A, DeleteById], value: Boolean): ElasticRequest[A, DeleteById] =
        request match {
          case Map(r, mapper)       => Map(withRefresh(r, value), mapper)
          case r: DeleteByIdRequest => r.copy(refresh = value)
        }
    }

    implicit val deleteByQueryWithRefresh: WithRefresh[DeleteByQuery] = new WithRefresh[DeleteByQuery] {
      def withRefresh[A](request: ElasticRequest[A, DeleteByQuery], value: Boolean): ElasticRequest[A, DeleteByQuery] =
        request match {
          case Map(r, mapper)          => Map(withRefresh(r, value), mapper)
          case r: DeleteByQueryRequest => r.copy(refresh = value)
        }
    }

    implicit val upsertWithRefresh: WithRefresh[Upsert] = new WithRefresh[Upsert] {
      def withRefresh[A](request: ElasticRequest[A, Upsert], value: Boolean): ElasticRequest[A, Upsert] =
        request match {
          case Map(r, mapper)           => Map(withRefresh(r, value), mapper)
          case r: CreateOrUpdateRequest => r.copy(refresh = value)
        }
    }
  }
}
