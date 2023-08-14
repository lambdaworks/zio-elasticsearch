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

You can find more information about `Refresh` request [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/indices-refresh.html).