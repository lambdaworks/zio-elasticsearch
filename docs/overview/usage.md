---
id: overview_usage
title: "Usage"
---

In order to execute an Elasticsearch request we can rely on the `Elasticsearch` layer which offers an `execute` method accepting an `ElasticRequest`. In order to build the `Elasticsearch` layer we need to provide the following layers:

- `ElasticExecutor`: if you provide `ElasticExecutor.local`, it will run on `localhost:9200`. Otherwise, if you want to use `ElasticExecutor.live`, you must also provide `ElasticConfig`.
- `HttpClientZioBackend`

```scala
import sttp.client3.httpclient.zio.HttpClientZioBackend
import zio.elasticsearch._
import zio._

object ZIOElasticsearchExample extends ZIOAppDefault {
  val indexName = IndexName("index")
  val result: RIO[Elasticsearch, CreationOutcome] =
    Elasticsearch.execute(ElasticRequest.createIndex(indexName))

  override def run =
    result.provide(
      ElasticExecutor.local,
      Elasticsearch.layer,
      HttpClientZioBackend.layer()
    )
}
```
