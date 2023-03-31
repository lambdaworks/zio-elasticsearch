---
id: overview_streaming
title: "Streaming"
---

ZIO Elasticsearch offers a few different API methods for creating ZIO streams out of search requests.
The library offers two different streaming modes relying on two different ways of retrieving paged results from Elasticsearch: `scroll` and `search_after`.
When using the `Elasticsearch.stream(...)` method you can provide your own configuration by creating the `StreamConfig` object and providing
it as a parameter for the method next to `SearchRequest`. If you choose not to provide `StreamConfig` then `StreamConfig.Default` will be used.

`StreamConfig.Default` uses Scroll API by default (which is recommended for queries that have under 10,000 results), has keep_alive parameter set for `1m` and
uses Elasticsearch default page size.

`StreamConfig` also makes use of our fluent API, so you can use methods `withPageSize` (used to determine how many documents to return per page) 
and `keepAliveFor` (used to tell Elasticsearch how long should search be kept alive after every pagination using [Time units](https://www.elastic.co/guide/en/elasticsearch/reference/8.6/api-conventions.html#time-units)).
`StreamConfig` has two predefined values for `StreamConfig.Scroll` that uses ElasticSearch Scroll API and `StreamConfig.SearchAfter` that uses Search After API with Point In Time.

```scala
StreamConfig(searchAfter = false, keepAlive = "5m", pageSize = Some(100))
```

When using the `streamAs[A]` method, results are parsed into the desired type `A`, relying on an implicit schema for `A`.

```scala
final case class User(id: Int, name: String)

object User {
  implicit val schema: Schema.CaseClass2[Int, String, User] =
    DeriveSchema.gen[User]

  val (id, name) = schema.makeAccessors(FieldAccessorBuilder)
}

val request: SearchRequest =
  ElasticRequest.search(IndexName("index"), ElasticQuery.range(User.id).gte(5))

val searchAfterStream: ZStream[Elasticsearch, Throwable, User] =
  Elasticsearch.streamAs[User](request, StreamConfig.SearchAfter)
```

Besides the type-safe `streamAs[A]` method, the library offers a basic `stream` method, which result will be a stream of type `Item` which contains a `raw` field that represents a document using the `Json` type from the ZIO JSON library.

```scala
val request: SearchRequest =
  ElasticRequest.search(IndexName("index"), ElasticQuery.range("id").gte(5))

val defaultStream: ZStream[Elasticsearch, Throwable, Item] =
  Elasticsearch.stream(request)

val scrollStream: ZStream[Elasticsearch, Throwable, Item]  =
  Elasticsearch.stream(request, StreamConfig.Scroll)
```
