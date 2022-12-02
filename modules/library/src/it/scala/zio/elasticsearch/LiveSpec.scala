package zio.elasticsearch

import sttp.client3.httpclient.zio.HttpClientZioBackend
import zio.test._

object LiveSpec extends ZIOSpecDefault {

  private val elasticsearchLayer = HttpClientZioBackend.layer() >>> ElasticExecutor.local

  override def spec: Spec[TestEnvironment, Any] =
    suite("live test")()
      .provideSomeLayerShared[TestEnvironment](
        elasticsearchLayer
      )
}
