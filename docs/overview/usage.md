---
id: overview_usage
title: "Usage"
---

In order to execute an Elasticsearch request we can rely on the `Elasticsearch` layer which offers an `execute` method accepting an `ElasticRequest`. In order to build the `Elasticsearch` layer we need to provide the following layers:

- `ElasticExecutor` - if you provide `ElasticExecutor.local` it will run on localhost:9200, otherwise if you want to use `ElasticExecutor.live` you will have to provide `ElasticConfig` as well
- `HttpClientZioBackend`

```scala
import sttp.client3.httpclient.zio.HttpClientZioBackend
import zio.elasticsearch.ElasticRequest._
import zio.elasticsearch._
import zio._

object ZIOElasticsearchExample extends ZIOAppDefault {
  val indexName = IndexName("index")
  val effect: RIO[Elasticsearch, Unit] = for {
    _ <- Elasticsearch.execute(createIndex(indexName))
  } yield ()

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    effect.provide(
      ElasticExecutor.local,
      Elasticsearch.layer,
      HttpClientZioBackend.layer()
    )
}
```
