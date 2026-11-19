---
id: elastic_request_multi_get
title: "Multi Get Request"
---

The `MultiGet` request retrieves multiple JSON documents by their IDs in a single request.

To create a `MultiGet` request for documents located in the same index do the following:
```scala
import zio.elasticsearch.ElasticRequest.MultiGetRequest
import zio.elasticsearch.ElasticRequest.multiGet
// this import is required for using `IndexName` and `DocumentId`
import zio.elasticsearch._

val request: MultiGetRequest = multiGet(index = IndexName("index"), id = DocumentId("111"), ids = DocumentId("222"), DocumentId("333"))
```

To retrieve documents located in different indices, pass pairs of index name and document ID:
```scala
val requestAcrossIndices: MultiGetRequest =
  multiGet((IndexName("index1"), DocumentId("111")), (IndexName("index2"), DocumentId("222")))
```

If you want to change the `refresh`, you can use `refresh`, `refreshFalse` or `refreshTrue` method:
```scala
val requestWithRefresh: MultiGetRequest = multiGet(index = IndexName("index"), id = DocumentId("111")).refresh(true)
val requestWithRefreshFalse: MultiGetRequest = multiGet(index = IndexName("index"), id = DocumentId("111")).refreshFalse
val requestWithRefreshTrue: MultiGetRequest = multiGet(index = IndexName("index"), id = DocumentId("111")).refreshTrue
```

If you want to change the `realtime`, you can use the `realtime` method:
```scala
val requestWithRealtime: MultiGetRequest = multiGet(index = IndexName("index"), id = DocumentId("111")).realtime(false)
```

If you want to change the `routing`, you can use the `routing` method:
```scala
// this import is required for `Routing` also
import zio.elasticsearch._

val requestWithRouting: MultiGetRequest = multiGet(index = IndexName("index"), id = DocumentId("111")).routing(Routing("routing"))
```

If you want to specify which fields of the documents are returned, you can use the `includes` and `excludes` methods:
```scala
val requestWithIncludes: MultiGetRequest = multiGet(index = IndexName("index"), id = DocumentId("111")).includes("name", "age")
val requestWithIncludesFromSchema: MultiGetRequest = multiGet(index = IndexName("index"), id = DocumentId("111")).includes[User]
val requestWithExcludes: MultiGetRequest = multiGet(index = IndexName("index"), id = DocumentId("111")).excludes("email")
```

Executing the `MultiGet` request returns a `MultiGetResult`. Found documents can be decoded with `documentAs`, while documents that were not found and documents that could not be retrieved (for example, because their index does not exist) are reported with `missing` and `failures` respectively:
```scala
import zio._
import zio.elasticsearch.result.{MultiGetItem, MultiGetResult}

val result: RIO[Elasticsearch, MultiGetResult] =
  Elasticsearch.execute(multiGet(index = IndexName("index"), id = DocumentId("111"), ids = DocumentId("222")))

val users: RIO[Elasticsearch, Chunk[User]] = result.documentAs[User]
val missing: RIO[Elasticsearch, Chunk[MultiGetItem.NotFound]] = result.flatMap(_.missing)
val failures: RIO[Elasticsearch, Chunk[MultiGetItem.Failed]] = result.flatMap(_.failures)
```

Use `items` to get every requested document, in the order it was requested, as a `MultiGetItem` that is either `Found`, `NotFound` or `Failed`.

You can find more information about `MultiGet` request [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/docs-multi-get.html).
