package zio.elasticsearch

import com.github.tomakehurst.wiremock.WireMockServer
import sttp.client3.httpclient.zio.HttpClientZioBackend
import zio.ZLayer
import zio.test.ZIOSpecDefault

trait WiremockSpec extends ZIOSpecDefault {
  val index: IndexName = IndexName("organization")

  val wireMockPort = 9300
  val elasticsearchWireMockLayer: ZLayer[Any, Throwable, ElasticExecutor] =
    HttpClientZioBackend
      .layer() >>> (ZLayer.succeed(ElasticConfig.apply("localhost", wireMockPort)) >>> ElasticExecutor.live)
  val wireMockServer = new WireMockServer(wireMockPort)
}
