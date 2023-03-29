---
id: overview_elastic_executor
title: "Elastic Executor"
---

In order to get the functional effect of executing a specified Elasticsearch request, you must call the `execute` method defined on it, which returns a `ZIO` that requires an `ElasticExecutor`, fails with a `Throwable` and returns the relevant value `A` for that request.
Elastic requests for creating and deleting return `CreationOutcome` and `DeletionOutcome` respectively if no other meaningful value could be returned, notifying us on the outcome of the request. Any other kind of error is returned as a `Throwable` in the error channel of `ZIO` for that Elastic request.
If you want to execute multiple Elasticsearch requests in a single API call, you need to use the `bulk` method on those Elastic requests, and call `execute` on that bulk request instead.
To provide the dependency on `ElasticExecutor`, you must pass one of the `ZLayer`s from the following import:

```scala
import zio.elasticsearch.ElasticExecutor
```

For example, if you want to execute requests on a server running on `localhost` and port `9200`, you can achieve that in two ways:
 - provide the `live` `ZLayer` to your effect, along with a `SttpBackend` and an `ElasticConfig` layer,
 - or provide `ElasticExecutor.local` layer along with a `SttpBackend`.

```scala
import sttp.client3.SttpBackend
import sttp.client3.httpclient.zio.HttpClientZioBackend
import zio.elasticsearch.{ElasticConfig, ElasticExecutor}

val effect: RIO[ElasticExecutor, Boolean] = exists(IndexName("index"), DocumentId("documentId")).execute

// Executing Elasticsearch requests with provided ElasticConfig layer explicitly
effect.provide(
  HttpClientZioBackend.layer(),
  ZLayer.succeed(ElastichConfig("localhost", 9200)) >>> ElasticExecutor.live
)

// Executing Elasticsearch requests with local ElasticExecutor
effect.provide(
  HttpClientZioBackend.layer(),
  ElasticExecutor.local
)
```
