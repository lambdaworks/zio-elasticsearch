---
id: overview_elastic_executor
title: "Executing Requests"
---

In order to get the functional effect of executing a specified Elasticsearch request, you should call the `execute` method defined in the `Elasticsearch`, which returns a `ZIO` that requires an `Elasticsearch`, fails with a `Throwable` and returns the relevant value `A` for that request.
The `Elasticsearch.layer` can be provided using the following import:

```scala
import zio.elasticsearch.Elasticsearch
```

However, `Elasticsearch.layer` requires a dependency on the `ElasticExector`.
To provide the dependency on `ElasticExecutor`, you must pass one of the `ZLayer`s from the following import:

```scala
import zio.elasticsearch.ElasticExecutor
```

For example, if you want to execute requests on an Elasticsearch server running on `localhost` and port `9200`, you can achieve that in two ways:
 - provide the `live` `ZLayer` to your effect, along with a `SttpBackend` and an `ElasticConfig` layer,
 - or provide `ElasticExecutor.local` layer along with a `SttpBackend`.

The recommended `SttpBackend` is `HttpClientZioBackend.layer()` from sttp.

As an opt-in alternative, `ElasticBackend.layer()` provides the same `HttpClientZioBackend`, configured so that the HTTP client runs its work on its selector thread instead of handing it off to another thread.
This saves one thread hop per request, which was about 3-4% faster in benchmarks over plain HTTP.
Because that thread does the I/O for all connections, CPU-heavy work such as TLS handshakes delays every in-flight request, so prefer `HttpClientZioBackend.layer()` for HTTPS clusters.

```scala
import sttp.client4.httpclient.zio.HttpClientZioBackend
import zio._
import zio.elasticsearch._

val result: RIO[Elasticsearch, Boolean] =
  Elasticsearch.execute(ElasticRequest.exists(IndexName("index"), DocumentId("documentId")))

// Executing Elasticsearch requests with provided ElasticConfig layer explicitly
result.provide(
  ZLayer.succeed(ElasticConfig("localhost", 9200)) >>> ElasticExecutor.live,
  Elasticsearch.layer,
  HttpClientZioBackend.layer()
)

// Executing Elasticsearch requests with local ElasticExecutor
result.provide(
  ElasticExecutor.local,
  Elasticsearch.layer,
  HttpClientZioBackend.layer()
)
```
