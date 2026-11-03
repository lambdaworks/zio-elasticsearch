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

import zio._
import zio.config.magnolia.deriveConfig

final case class AppConfig(http: HttpConfig, elasticsearch: ElasticsearchConfig)

object AppConfig {
  private[this] final val config = ZLayer(ZIO.config(deriveConfig[AppConfig]))

  lazy val live: Layer[Config.Error, ElasticsearchConfig & HttpConfig] =
    config.project(_.elasticsearch) ++ config.project(_.http)
}
