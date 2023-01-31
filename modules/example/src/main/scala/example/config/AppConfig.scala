/*
 * Copyright 2022 LambdaWorks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package example.config

import zio.Layer
import zio.config.ReadError
import zio.config.magnolia.descriptor
import zio.config.syntax._
import zio.config.typesafe.TypesafeConfig

final case class AppConfig(http: HttpConfig, elasticsearch: ElasticsearchConfig)

object AppConfig {
  lazy val live: Layer[ReadError[String], ElasticsearchConfig with HttpConfig] = {
    val config = TypesafeConfig.fromResourcePath(descriptor[AppConfig])
    config.narrow(_.elasticsearch) >+> config.narrow(_.http)
  }
}
