---
id: overview_streaming
title: "Streaming"
---

Zio-elastic search is streaming friendly library and there are few specific API's that are used for creating ZIO streams. When using `stream` the result will be `Item` that is case class that contains only one field and that is `raw` that represents your response as raw JSON. Also it is important to note that you can use `StramConfig` to use your own settings when creating a stream, if you omit using `StreamConfig` then `StreamConfig.Default` will be used.

```scala
for {
  stream <- Elasticsearch.stream(ElasticRequest.search(secondSearchIndex, range("id").gte(5)))
  stream <- Elasticsearch.stream(ElasticRequest.search(secondSearchIndex, range("id").gte(5)), StreamConfig.Scroll)
} yield ()
```

```scala
for {
  stream <- Elasticsearch.streamAs[User](ElasticRequest.search(secondSearchIndex, range(User.id).gte(5)))
  stream <- Elasticsearch.streamAs[User](ElasticRequest.search(secondSearchIndex, range(User.id).gte(5)), StreamConfig.SearchAfter)
} yield ()
```

