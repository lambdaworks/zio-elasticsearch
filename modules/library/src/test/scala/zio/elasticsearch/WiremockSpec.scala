package zio.elasticsearch

import com.github.tomakehurst.wiremock.WireMockServer
import sttp.client3.httpclient.zio.HttpClientZioBackend
import zio.ZLayer
import zio.test.ZIOSpecDefault

trait WiremockSpec extends ZIOSpecDefault {
  val index: IndexName = IndexName("organization")

  val gitHubRepo: GitHubRepo =
    GitHubRepo(id = Some("123"), organization = "lambdaworks.io", name = "LambdaWorks", stars = 10, forks = 10)

  val port: Int = 5700
  val elasticsearchWireMockLayer: ZLayer[Any, Throwable, ElasticExecutor] =
    HttpClientZioBackend
      .layer() >>> (ZLayer.succeed(ElasticConfig.apply("localhost", port)) >>> ElasticExecutor.live)
  val server: WireMockServer = new WireMockServer(port)
}
