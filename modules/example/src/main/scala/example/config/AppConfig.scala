package example.config

import zio.Layer
import zio.config.ReadError
import zio.config.magnolia.descriptor
import zio.config.syntax._
import zio.config.typesafe.TypesafeConfig

final case class AppConfig(http: HttpConfig, elasticsearch: ElasticsearchConfig)

object AppConfig {
  lazy val live: Layer[ReadError[String], AppConfig with ElasticsearchConfig with HttpConfig] = {
    val config = TypesafeConfig.fromResourcePath(descriptor[AppConfig])
    config >+> config.narrow(_.elasticsearch) >+> config.narrow(_.http)
  }
}
