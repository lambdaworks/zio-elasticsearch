package zio.elasticsearch

import zio.elasticsearch.ElasticRequest._
import zio.elasticsearch.ElasticRequestType.{Create, DeleteById, Exists, GetById, Upsert}
import zio.prelude.Assertion.isEmptyString
import zio.prelude.Newtype

object Routing extends Newtype[String] {
  override def assertion = assert(!isEmptyString) // scalafix:ok

  type Routing = Routing.Type

  trait WithRouting[ERT <: ElasticRequestType] {
    def withRouting[A](request: ElasticRequest[A, ERT], routing: Routing): ElasticRequest[A, ERT]
  }

  object WithRouting {
    implicit val createWithRouting: WithRouting[Create] = new WithRouting[Create] {
      def withRouting[A](request: ElasticRequest[A, Create], routing: Routing): ElasticRequest[A, Create] =
        request match {
          case Map(r, mapper)   => Map(withRouting(r, routing), mapper)
          case r: CreateRequest => r.copy(routing = Some(routing))
        }
    }

    implicit val deleteByIdWithRouting: WithRouting[DeleteById] = new WithRouting[DeleteById] {
      def withRouting[A](request: ElasticRequest[A, DeleteById], routing: Routing): ElasticRequest[A, DeleteById] =
        request match {
          case Map(r, mapper)       => Map(withRouting(r, routing), mapper)
          case r: DeleteByIdRequest => r.copy(routing = Some(routing))
        }
    }

    implicit val existsWithRouting: WithRouting[Exists] = new WithRouting[Exists] {
      def withRouting[A](request: ElasticRequest[A, Exists], routing: Routing): ElasticRequest[A, Exists] =
        request match {
          case Map(r, mapper)   => Map(withRouting(r, routing), mapper)
          case r: ExistsRequest => r.copy(routing = Some(routing))
        }
    }

    implicit val getByIdWithRouting: WithRouting[GetById] = new WithRouting[GetById] {
      def withRouting[A](request: ElasticRequest[A, GetById], routing: Routing): ElasticRequest[A, GetById] =
        request match {
          case Map(r, mapper)    => Map(withRouting(r, routing), mapper)
          case r: GetByIdRequest => r.copy(routing = Some(routing))
        }
    }

    implicit val upsertWithRouting: WithRouting[Upsert] = new WithRouting[Upsert] {
      def withRouting[A](request: ElasticRequest[A, Upsert], routing: Routing): ElasticRequest[A, Upsert] =
        request match {
          case Map(r, mapper)           => Map(withRouting(r, routing), mapper)
          case r: CreateOrUpdateRequest => r.copy(routing = Some(routing))
        }
    }
  }
}
