package zio.elasticsearch

import com.github.tomakehurst.wiremock.WireMockServer
import sttp.client3.httpclient.zio.HttpClientZioBackend
import zio.test.ZIOSpecDefault
import zio.{TaskLayer, ZLayer}

trait WiremockSpec extends ZIOSpecDefault {
  val index: IndexName = IndexName("repositories")

  val repo: GitHubRepo =
    GitHubRepo(id = Some("123"), organization = "lambdaworks.io", name = "LambdaWorks", stars = 10, forks = 10)

  val port: Int = 9300
  val elasticsearchWireMockLayer: TaskLayer[ElasticExecutor] =
    HttpClientZioBackend
      .layer() >>> (ZLayer.succeed(ElasticConfig.apply("localhost", port)) >>> ElasticExecutor.live)
  val server: WireMockServer = new WireMockServer(port)
}
