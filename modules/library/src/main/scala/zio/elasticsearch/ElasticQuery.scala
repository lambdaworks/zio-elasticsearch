package zio.elasticsearch

import zio.json.ast.Json
import zio.json.ast.Json.{Arr, Bool, Num, Obj, Str}

sealed trait ElasticQuery { self =>

  def asJson: Json

  final def asJsonBody: Json = Obj("query" -> self.asJson)

}

object ElasticQuery {

  def matches(field: String, query: String): ElasticQuery =
    Match(field, query)

  def matches(field: String, query: Boolean): ElasticQuery =
    Match(field, query)

  def matches(field: String, query: Long): ElasticQuery =
    Match(field, query)

  def boolQuery(): BoolQuery = BoolQuery.empty

  private[elasticsearch] final case class BoolQuery(must: List[ElasticQuery], should: List[ElasticQuery])
      extends ElasticQuery { self =>

    override def asJson: Json =
      Obj("bool" -> Obj("must" -> Arr(must.map(_.asJson): _*), "should" -> Arr(should.map(_.asJson): _*)))

    def must(queries: ElasticQuery*): BoolQuery =
      self.copy(must = must ++ queries)

    def should(queries: ElasticQuery*): BoolQuery =
      self.copy(should = should ++ queries)
  }

  object BoolQuery {
    def empty: BoolQuery = BoolQuery(Nil, Nil)
  }

  private[elasticsearch] final case class Match[A](field: String, query: A) extends ElasticQuery {
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
