---
id: elastic_request_delete_index
title: "Delete Index Request"
---

This request deletes specified Elasticsearch index.

To create a `DeleteById` request do the following:
```scala
import zio.elasticsearch.ElasticRequest.DeleteIndexRequest
import zio.elasticsearch.ElasticRequest.deleteIndex
// this import is required for using `IndexName`
import zio.elasticsearch._

val request: DeleteIndexRequest = deleteIndex(name = IndexName("index"))
```

You can find more information about `DeleteIndex` request [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/indices-delete-index.html).
