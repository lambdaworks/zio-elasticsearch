---
id: elastic_request_create_index
title: "Create Index Request"
---

This request creates a new Elasticsearch index.

In order to use the `CreateIndex` request import the following:
```scala
import zio.elasticsearch.ElasticRequest.CreateIndexRequest
import zio.elasticsearch.ElasticRequest.createIndex
```

You can create a `CreateIndex` request using the `createIndex` method in the following manner:
```scala
// this import is required for using `IndexName`
import zio.elasticsearch._

val request: CreateIndexRequest = createIndex(name = IndexName("index"))
```

You can also create a `CreateIndex` request using the `createIndex` method with the specific definition in the following manner:
```scala
val request: CreateIndexRequest = createIndex(name = IndexName("index"), definition = """{ "mappings": { "properties": { "subDocumentList": { "type": "nested" } } } }""")
```

You can find more information about `CreateIndex` request [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/indices-create-index.html).
