---
id: elastic_request_refresh
title: "Refresh Request"
---

This request is used for refreshing Elasticsearch index.

In order to use the `Refresh` request import the following:
```scala
import zio.elasticsearch.ElasticRequest.RefreshRequest
import zio.elasticsearch.ElasticRequest.refresh
```

You can create a `Refresh` request using the `refresh` method in the following manner:
```scala
// this import is required for using `IndexName`
import zio.elasticsearch._

val request: RefreshRequest = refresh(index = IndexName("index"))
```

If you want to refresh more indices, you can use `refresh` method this way:
```scala
val request: RefreshRequest = refresh(index = MultiIndex.names(IndexName("index1"), IndexName("index2")))
```

If you want to refresh all indices, you can use `refresh` method with `IndexPattern` this way:
```scala
val request: RefreshRequest = refresh(index = IndexPattern("_all"))
```

You can find more information about `Refresh` request [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/indices-refresh.html).

