package zio.elasticsearch

import com.github.tomakehurst.wiremock.WireMockServer
import sttp.client3.httpclient.zio.HttpClientZioBackend
import zio.prelude.Newtype.unsafeWrap
import zio.test.ZIOSpecDefault
import zio.{TaskLayer, ZIO, ZLayer}

trait WireMockSpec extends ZIOSpecDefault {
  val index: IndexName = unsafeWrap(IndexName)("repositories")

  val repo: GitHubRepo =
    GitHubRepo(id = Some("123"), organization = "lambdaworks.io", name = "LambdaWorks", stars = 10, forks = 10)

  val port: Int = 9300

  val elasticsearchWireMockLayer: TaskLayer[ElasticExecutor] =
    HttpClientZioBackend
      .layer() >>> (ZLayer.succeed(ElasticConfig.apply("localhost", port)) >>> ElasticExecutor.live)

  val wireMockServerLayer: TaskLayer[WireMockServer] = {
    val server = ZIO.acquireRelease(
      ZIO.attemptBlocking {
        val server = new WireMockServer(port)

        server.start()
        server
      }
    )(server => ZIO.succeedBlocking(server.stop()))

    ZLayer.scoped(server)
  }
}
