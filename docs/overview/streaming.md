---
id: overview_streaming
title: "Streaming"
---

Zio-elastic search is streaming friendly library and there are few specific API's that are used for creating ZIO streams. 
Library makes use of two Elasticsearch APIs to create ZIO Stream, and those are Scroll API and Search_after API. 
When using `Elasticsearch.stream()` method you can provide your own configuration by creating `StreamConfig` object and providing
it as parameter for method next to `SearchRequest`. If you choose not to provide `StreamConfig` then `StreamConfig.Default` will be used.

`StreamConfig.Default` uses Scroll API by default(which is recommended for queries that have under 10,000 results), has keep_alive parameter set for `1m` and
uses Elasticsearch default page size.

`StreamConfig` also makes use of our fluent API, so you can use methods `withPageSize` (used to determine how many documents to return per page) 
and `keepAliveFor` (used to tell Elasticsearch how long should search be kept alive after every pagination using [Time units](https://www.elastic.co/guide/en/elasticsearch/reference/8.6/api-conventions.html#time-units), so it is not removed in between requests if your stream processing takes longer than that).
`StreamConfig` has two predefined values for `StreamConfig.Scroll` that uses ElasticSearch Scroll API and `StreamConfig.SearchAfter` that uses Search After API with Point In Time.

```scala
StreamConfig(searchAfter = false, keepAlive = "5m", pageSize = Some(100))
```

When using `stream` method the result will be `Item` case class that contains only one field and that is `raw` field which 
represents your document as raw Json object from ZIO Json library. 

```scala
for {
  stream <- Elasticsearch.stream(ElasticRequest.search(secondSearchIndex, range("id").gte(5)))
  stream <- Elasticsearch.stream(ElasticRequest.search(secondSearchIndex, range("id").gte(5)), StreamConfig.Scroll)
} yield ()
```

Other than basic `stream` method, library has `streamAs` method that requires case class that has implicit schema defined and
uses ZIO Schema to convert response to case class you provided.

```scala
case class User(id: Int, name: String)

object User {
  implicit val schema: Schema[User] = DeriveSchema.gen[User]
}

for {
  stream <- Elasticsearch.streamAs[User](ElasticRequest.search(secondSearchIndex, range(User.id).gte(5)))
  stream <- Elasticsearch.streamAs[User](ElasticRequest.search(secondSearchIndex, range(User.id).gte(5)), StreamConfig.SearchAfter)
} yield ()
```
