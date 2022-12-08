package example

import zio.elasticsearch.ElasticQuery._

object Main extends App {
  val simpleAnd = matches("day_of_week", "Monday") and matches("customer_gender", "MALE")
  println(simpleAnd.asJson)

  val simpleOr = matches("day_of_week", "Monday") or matches("customer_gender", "MALE")
  println(simpleOr.asJson)

  val query1 =
    matches("day_of_week", "Monday") and matches("customer_gender", "MALE") or matches("discount_percentage", 0)
  println(query1.asJson)
  println(query1)

  val query2 =
    matches("tax_amount", 0) or matches("category", "Men's Clothing") and matches(
      "manufacturer",
      "Elitelligence"
    ) and query1 or simpleAnd and simpleOr
  println(query2.asJson)

}
