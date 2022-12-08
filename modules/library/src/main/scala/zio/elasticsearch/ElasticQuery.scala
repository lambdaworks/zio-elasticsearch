package zio.elasticsearch

import zio.elasticsearch.ElasticQuery.{And, Or}
import zio.json.ast.Json
import zio.json.ast.Json.{Arr, Bool, Num, Obj, Str}

sealed trait ElasticQuery { self =>

  def asJson: Json

  final def and(other: ElasticQuery): ElasticQuery =
    And(self, other)

  final def or(other: ElasticQuery): ElasticQuery =
    Or(self, other)

  final def asJsonBody: Json = Obj("query" -> self.asJson)

}

object ElasticQuery {

  def matches(field: String, query: String): ElasticQuery =
    Match(field, query)

  def matches(field: String, query: Boolean): ElasticQuery =
    Match(field, query)

  def matches(field: String, query: Long): ElasticQuery =
    Match(field, query)

  private[elasticsearch] final case class And(query: ElasticQuery, other: ElasticQuery) extends ElasticQuery {
    override def asJson: Json =
      Obj("bool" -> Obj("must" -> Arr(query.asJson, other.asJson)))
  }

  private[elasticsearch] final case class Or(query: ElasticQuery, other: ElasticQuery) extends ElasticQuery {
    override def asJson: Json =
      Obj("bool" -> Obj("should" -> Arr(query.asJson, other.asJson)))
  }

  private[elasticsearch] case class Match[A](field: String, query: A) extends ElasticQuery {
    override def asJson: Json =
      query match {
        case str if str.isInstanceOf[String] =>
          Obj("match" -> Obj(field -> Str(str.asInstanceOf[String])))
        case bool if bool.isInstanceOf[Boolean] =>
          Obj("match" -> Obj(field -> Bool(bool.asInstanceOf[Boolean])))
        case num if num.isInstanceOf[Long] =>
          Obj("match" -> Obj(field -> Num(num.asInstanceOf[Long])))
      }
  }

}
