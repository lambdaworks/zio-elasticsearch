---
id: overview_usage
title: "Usage"
---

In order to get the functional effect of executing a specified `Elasticsearch` request, we must provide the `Elasticsearch` layer to that effect.  We can then call the `execute`method defined in `Elasticsearch` that accepts `ElasticRequest` as parameter.
To create this layer we also have to provide following layers:

- `ElasticExecutor` - if you provide `ElasticExecutor.local` it will run on localhost:9200, otherwise if you want to use `ElasticExecutor.live` you will have to provide `ElasticConfig` as well
- `HttpClientZioBackend`

```scala
import sttp.client3.httpclient.zio.HttpClientZioBackend
import zio.elasticsearch.ElasticRequest._
import zio.elasticsearch._
import zio._

object ZIOElasticsearchExample extends ZIOAppDefault {
  val indexName = IndexName("test-es-index")
  val effect: ZIO[Elasticsearch, Throwable, Unit] = for {
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
