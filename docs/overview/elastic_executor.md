---
id: overview_elastic_executor
title: "Elastic Executor"
---

In order to get the functional effect of executing a specified Elasticsearch request, you must call the `execute` method defined on it, which returns a `ZIO` that requires an `ElasticExecutor`, fails with a `Throwable` and returns the relevant value `A` for that request.

Elastic requests for creating and deleting return `CreationOutcome` and `DeletionOutcome` respectively if no other meaningful value could be returned, notifying us on the success of the request. Any other kind of error is returned as a `Throwable` in the error channel of `ZIO` for that Elastic request.

If you want to execute multiple Elasticsearch requests in a single API call, you need to use the `bulk` method on those Elastic requests, and call `execute` on that bulk request instead.

To provide the dependency on `ElasticExecutor`, you must pass one of the `ZLayer`s from the following import:

```scala
import zio.elasticsearch.ElasticExecutor
```

For example, if you want to execute requests on a server running on `localhost` and port `9200`, you can provide the `live` ZLayer to your effect, along with a `SttpBackend` and an `ElasticConfig` layer:

```scala
import sttp.client3.SttpBackend
import sttp.client3.httpclient.zio.HttpClientZioBackend
import zio.elasticsearch.{ElasticConfig, ElasticExecutor}

val effect: RIO[ElasticExecutor, Boolean] = exists(IndexName("index"), DocumentId("document")).execute

effect.provide(
  HttpClientZioBackend.layer(),
  ZLayer.succeed(ElastichConfig("localhost", 9200)) >>> ElasticExecutor.live,
)
```

If the ElasticConfig arguments are the same as specified above, you can simply omit the `ElasticConfig` layer and replace `ElasticExecutor.live` with `ElasticExecutor.local` instead.

For testing purposes, you can use `ElasticExecutor.test`, which is a mocked Elasticsearch executor that doesn't require an HTTP backend.

```scala
// The Elasticsearch requests are executed locally
effect.provide(
  HttpClientZioBackend.layer(),
  ElasticExecutor.local
)

// The Elasticsearch requests are executed on a mocked executor
effect.provideLayer(ElasticExecutor.test)
```
