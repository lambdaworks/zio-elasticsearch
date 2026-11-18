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

package zio.elasticsearch

import sttp.client4.BackendOptions
import sttp.client4.httpclient.zio.{HttpClientZioBackend, SttpClient}
import zio._

import java.net.http.HttpClient
import java.net.{Authenticator, PasswordAuthentication}
import java.time.{Duration => JDuration}
import java.util.concurrent.Executor

object ElasticBackend {

  /**
   * An [[sttp.client4.httpclient.zio.SttpClient]] tuned for Elasticsearch.
   *
   * Unlike `HttpClientZioBackend.layer()`, the underlying `java.net.http.HttpClient` completes responses directly on
   * its selector thread instead of handing them off to another executor first. The response body is consumed as a
   * reactive stream that only enqueues data, so no blocking work runs on that thread, and every request saves one
   * thread hop.
   *
   * @param options
   *   the [[sttp.client4.BackendOptions]] used to configure the connection timeout and proxy
   * @return
   *   a scoped layer that closes the client when released.
   */
  def layer(options: BackendOptions = BackendOptions.Default): TaskLayer[SttpClient] =
    ZLayer.scoped(ZIO.attempt(client(options)).flatMap(HttpClientZioBackend.scopedUsingClient(_)))

  private def client(options: BackendOptions): HttpClient = {
    val builder =
      HttpClient
        .newBuilder()
        .followRedirects(HttpClient.Redirect.NEVER)
        .connectTimeout(JDuration.ofMillis(options.connectionTimeout.toMillis))
        .executor(DirectExecutor)

    options.proxy match {
      case Some(proxy) =>
        proxy.auth
          .fold(builder.proxy(proxy.asJavaProxySelector))(auth =>
            builder.proxy(proxy.asJavaProxySelector).authenticator(new ProxyAuthenticator(auth.username, auth.password))
          )
          .build()
      case None =>
        builder.build()
    }
  }

  private final class ProxyAuthenticator(username: String, password: String) extends Authenticator {
    override def getPasswordAuthentication: PasswordAuthentication =
      if (getRequestorType == Authenticator.RequestorType.PROXY)
        new PasswordAuthentication(username, password.toCharArray)
      else null
  }

  private object DirectExecutor extends Executor {
    def execute(command: Runnable): Unit = command.run()
  }
}
