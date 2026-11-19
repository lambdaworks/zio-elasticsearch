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

import sttp.capabilities.zio.ZioStreams
import sttp.client4.BackendOptions
import sttp.client4.compression.CompressionHandlers
import sttp.client4.httpclient.zio.{HttpClientZioBackend, SttpClient}
import zio._
import zio.stream.ZStream

import java.net.http.{HttpClient, HttpRequest}
import java.net.{Authenticator, PasswordAuthentication}
import java.time.{Duration => JDuration}
import java.util.concurrent.Executor

object ElasticBackend {

  /**
   * An [[sttp.client4.httpclient.zio.SttpClient]] tuned for Elasticsearch, offered as an opt-in alternative to
   * `HttpClientZioBackend.layer()`.
   *
   * Unlike `HttpClientZioBackend.layer()`, the underlying `java.net.http.HttpClient` runs the work it would otherwise
   * hand off to an executor directly on its selector thread, which saves one thread hop per request. The response body
   * is consumed as a reactive stream that only enqueues data, so completing responses does not block that thread.
   *
   * The selector thread does the I/O for all connections, so anything CPU-heavy the client runs there delays every
   * in-flight request. For example, TLS handshakes run their delegated `SSLEngine` tasks on it. The gain was measured
   * over plain HTTP only, so prefer `HttpClientZioBackend.layer()` for HTTPS clusters.
   *
   * @param options
   *   the [[sttp.client4.BackendOptions]] used to configure the connection timeout and proxy
   * @param customizeRequest
   *   a function applied to every `java.net.http.HttpRequest` before it is sent, for example to add headers or set a
   *   request timeout
   * @param compressionHandlers
   *   the handlers used to compress request bodies and decompress response bodies
   * @return
   *   a layer that provides the [[sttp.client4.httpclient.zio.SttpClient]]. As with `HttpClientZioBackend.layer()`, the
   *   underlying `java.net.http.HttpClient` is not closed when the layer is released.
   */
  def layer(
    options: BackendOptions = BackendOptions.Default,
    customizeRequest: HttpRequest => HttpRequest = identity,
    compressionHandlers: CompressionHandlers[ZioStreams, ZStream[Any, Throwable, Byte]] =
      HttpClientZioBackend.DefaultCompressionHandlers
  ): TaskLayer[SttpClient] =
    ZLayer.scoped(
      ZIO
        .attempt(client(options))
        .flatMap(HttpClientZioBackend.scopedUsingClient(_, customizeRequest, compressionHandlers))
    )

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
