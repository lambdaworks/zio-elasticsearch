package zio.elasticsearch

final case class ElasticConfig(host: String, port: Int)

object ElasticConfig {
  lazy val Default: ElasticConfig = ElasticConfig("localhost", 9200)
  lazy val Uri: String            = "http://localhost:9200"
}
