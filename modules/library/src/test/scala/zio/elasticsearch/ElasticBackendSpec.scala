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

import com.sun.net.httpserver.{HttpExchange, HttpServer}
import sttp.client4.httpclient.zio.SttpClient
import sttp.client4.{UriContext, basicRequest}
import zio._
import zio.test.Assertion.{equalTo, isSome}
import zio.test._

import java.net.InetSocketAddress
import java.net.http.HttpRequest
import java.nio.charset.StandardCharsets

object ElasticBackendSpec extends ZIOSpecDefault {

  def spec: Spec[TestEnvironment, Any] =
    suite("ElasticBackend")(
      test("apply customizeRequest to every request") {
        ZIO.scoped {
          for {
            received <- Ref.make(Option.empty[String])
            runtime  <- ZIO.runtime[Any]
            server   <- ZIO.acquireRelease(ZIO.attempt(startServer(received, runtime)))(s => ZIO.succeed(s.stop(0)))
            client   <- ZLayer
                        .make[SttpClient](
                          ElasticBackend.layer(customizeRequest =
                            request => HttpRequest.newBuilder(request, (_, _) => true).header(Header, "value").build()
                          )
                        )
                        .build
                        .map(_.get[SttpClient])
            response <- basicRequest.get(uri"http://localhost:${server.getAddress.getPort}/").send(client)
            header   <- received.get
          } yield assert(response.code.code)(equalTo(200)) && assert(header)(isSome(equalTo("value")))
        }
      }
    )

  private final val Header = "X-Elastic-Backend-Test"

  private def startServer(received: Ref[Option[String]], runtime: Runtime[Any]): HttpServer = {
    val server = HttpServer.create(new InetSocketAddress("localhost", 0), 0)
    server.createContext(
      "/",
      (exchange: HttpExchange) => {
        Unsafe.unsafe { implicit unsafe =>
          runtime.unsafe.run(received.set(Option(exchange.getRequestHeaders.getFirst(Header)))).getOrThrowFiberFailure()
        }
        val body = "{}".getBytes(StandardCharsets.UTF_8)
        exchange.sendResponseHeaders(200, body.length.toLong)
        exchange.getResponseBody.write(body)
        exchange.close()
      }
    )
    server.start()
    server
  }
}
