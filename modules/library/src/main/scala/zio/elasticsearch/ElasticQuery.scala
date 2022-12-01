package zio.elasticsearch

import zio.elasticsearch.ElasticQuery.{And, Or}
import zio.json.ast.Json
import zio.json.ast.Json.{Arr, Bool, Num, Obj, Str}

sealed trait ElasticQuery { self =>

  def asJson: Json

  final def and(other: ElasticQuery): ElasticQuery =
    self match {
      case And(query, queries) => And(query, queries :+ other)
      case _                   => And(self, other :: Nil)
    }

  final def or(other: ElasticQuery): ElasticQuery =
    self match {
      case Or(query, queries) => Or(query, queries :+ other)
      case _                  => Or(self, other :: Nil)
    }
}

object ElasticQuery {

  def matches(field: String, query: String): ElasticQuery =
    Match(field, query)

  def matches(field: String, query: Boolean): ElasticQuery =
    Match(field, query)

  def matches(field: String, query: Long): ElasticQuery =
    Match(field, query)

  private[elasticsearch] final case class And(query: ElasticQuery, queries: List[ElasticQuery]) extends ElasticQuery {
    override def asJson: Json =
      Obj("bool" -> Obj("must" -> Arr((query +: queries).map(_.asJson): _*)))
  }

  private[elasticsearch] final case class Or(query: ElasticQuery, queries: List[ElasticQuery]) extends ElasticQuery {
    override def asJson: Json =
      Obj("bool" -> Obj("should" -> Arr((query +: queries).map(_.asJson): _*)))
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
