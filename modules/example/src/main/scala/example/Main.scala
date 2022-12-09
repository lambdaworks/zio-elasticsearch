package example

import sttp.client3.httpclient.zio.HttpClientZioBackend
import zio.elasticsearch.ElasticQuery._
import zio.elasticsearch.ElasticRequest.search
import zio.elasticsearch.{ElasticExecutor, IndexName}
import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault}

object Main extends ZIOAppDefault {
//  val simpleAnd = matches("day_of_week", "Monday") and matches("customer_gender", "MALE")
//  println(simpleAnd.asJson)
//
//  val simpleOr = matches("day_of_week", "Monday") or matches("customer_gender", "MALE")
//  println(simpleOr.asJson)
//
//  val query1 =
//    matches("day_of_week", "Monday") and matches("customer_gender", "MALE") or matches("discount_percentage", 0)
//  println(query1.asJson)
//  println(query1)
//
//  val query2 =
//    matches("tax_amount", 0) and matches("category", "Men's Clothing") or matches(
//      "manufacturer",
//      "Elitelligence"
//    ) // and query1 or simpleAnd and simpleOr
//  println(query2.asJsonBody)
//
//  val query2 =
//    (matches("customer_gender", "MALE") and matches("day_of_week", "Monday")) or matches("customer_last_name", "Weber")
//  println(query2.asJsonBody)

  val query =
    boolQuery()
      .must(matches("customer_last_name", "Weber"))

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = (for {
    _        <- ZIO.succeed("Executing query...")
    response <- search(IndexName(""), query).execute
  } yield response).provide(ElasticExecutor.local, HttpClientZioBackend.layer())
}
