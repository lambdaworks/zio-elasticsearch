package zio.elasticsearch

import sttp.model.Uri

final case class ElasticConfig(host: String, port: Int) {
  lazy val uri: Uri = Uri(host, port)
}

object ElasticConfig {
  lazy val Default: ElasticConfig = ElasticConfig("localhost", 9301)
}
