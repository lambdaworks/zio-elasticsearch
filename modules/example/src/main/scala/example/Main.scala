package example

import zio.elasticsearch.ElasticQuery._

object Main extends App {

  val query1 = matches("field", "or") or (matches("name", "zio-redis") and matches("starred", true) and matches("stars", 22))
  println(query1.asJson)

  val query2 = matches("field", "or") or matches("name", "zio-redis") and matches("starred", true) and matches("stars", 22)
  println(query2.asJson)

}
