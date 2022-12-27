package zio.elasticsearch

import zio.elasticsearch.ElasticRequest._
import zio.elasticsearch.ElasticRequestType.{Create, DeleteById, Exists, GetById, Upsert}
import zio.prelude.Assertion.isEmptyString
import zio.prelude.Newtype

object Routing extends Newtype[String] {
  override def assertion = assert(!isEmptyString) // scalafix:ok

  type Routing = Routing.Type

  trait WithRouting[T <: ElasticRequestType] {
    def withRouting[A](request: ElasticRequest[A, T], routing: Routing): ElasticRequest[A, T]
  }

  object WithRouting {
    implicit val addRoutingToCreate: WithRouting[Create] = new WithRouting[Create] {
      override def withRouting[A](req: ElasticRequest[A, Create], routing: Routing): ElasticRequest[A, Create] =
        req match {
          case Map(r, mapper)   => Map(withRouting(r, routing), mapper)
          case r: CreateRequest => r.copy(routing = Some(routing))
        }
    }
    implicit val addRoutingToDeleteById: WithRouting[DeleteById] = new WithRouting[DeleteById] {
      override def withRouting[A](
        req: ElasticRequest[A, DeleteById],
        routing: Routing
      ): ElasticRequest[A, DeleteById] =
        req match {
          case Map(r, mapper)       => Map(withRouting(r, routing), mapper)
          case r: DeleteByIdRequest => r.copy(routing = Some(routing))
        }
    }
    implicit val addRoutingToExists: WithRouting[Exists] = new WithRouting[Exists] {
      override def withRouting[A](req: ElasticRequest[A, Exists], routing: Routing): ElasticRequest[A, Exists] =
        req match {
          case Map(r, mapper)   => Map(withRouting(r, routing), mapper)
          case r: ExistsRequest => r.copy(routing = Some(routing))
        }
    }
    implicit val addRoutingToGetById: WithRouting[GetById] = new WithRouting[GetById] {
      override def withRouting[A](
        req: ElasticRequest[A, GetById],
        routing: Routing
      ): ElasticRequest[A, GetById] =
        req match {
          case Map(r, mapper)    => Map(withRouting(r, routing), mapper)
          case r: GetByIdRequest => r.copy(routing = Some(routing))
        }
    }
    implicit val addRoutingToUpsert: WithRouting[Upsert] = new WithRouting[Upsert] {
      override def withRouting[A](req: ElasticRequest[A, Upsert], routing: Routing): ElasticRequest[A, Upsert] =
        req match {
          case Map(r, mapper)           => Map(withRouting(r, routing), mapper)
          case r: CreateOrUpdateRequest => r.copy(routing = Some(routing))
        }
    }
  }
}
