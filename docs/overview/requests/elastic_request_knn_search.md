---
id: elastic_request_knn_search
title: "kNN Search Request"
---

The `kNN Search` request performs a k-nearest neighbor (kNN) search: given a query vector, it finds the `k` closest vectors and returns those documents as search hits.

In order to use the `kNN Search` request, the searched field must be mapped as a `dense_vector` field with indexing enabled, for example:
```scala
import zio.elasticsearch.ElasticRequest.{createIndex, CreateIndexRequest}
import zio.elasticsearch._

val createIndexRequest: CreateIndexRequest =
  createIndex(
    index = IndexName("index"),
    definition = """{ "mappings": { "properties": { "vectorField": { "type": "dense_vector", "dims": 3, "similarity": "l2_norm", "index": true } } } }"""
  )
```

To create a `kNN Search` request do the following:
```scala
import zio.Chunk
import zio.elasticsearch.ElasticRequest.KNNRequest
import zio.elasticsearch.ElasticRequest.knnSearch
// this import is required for using `IndexName`, `IndexPattern` and `MultiIndex`
import zio.elasticsearch._
import zio.elasticsearch.ElasticQuery._

val request: KNNRequest =
  knnSearch(selectors = IndexName("index"), query = kNN(field = Document.vectorField, k = 2, numCandidates = 5, queryVector = Chunk(1.0, 2.0, 3.0)))
```

The `query` parameter of the `knnSearch` method is a [`kNN`](https://lambdaworks.github.io/zio-elasticsearch/overview/queries/elastic_query_knn) query.

If you want to filter the documents that can match, you can use the `filter` method with any query:
```scala
val requestWithFilter: KNNRequest =
  knnSearch(selectors = IndexName("index"), query = kNN(field = Document.vectorField, k = 2, numCandidates = 5, queryVector = Chunk(1.0, 2.0, 3.0)))
    .filter(range(Document.intField).gt(10))
```

If you want to change the `routing`, you can use the `routing` method:
```scala
// this import is required for using `Routing` also
import zio.elasticsearch._

val requestWithRouting: KNNRequest =
  knnSearch(selectors = IndexName("index"), query = kNN(field = Document.vectorField, k = 2, numCandidates = 5, queryVector = Chunk(1.0, 2.0, 3.0)))
    .routing(Routing("routing"))
```

If you want to create `kNN Search` request with `IndexPattern`, do the following:
```scala
val requestWithIndexPattern: KNNRequest =
  knnSearch(selectors = IndexPattern("index*"), query = kNN(field = Document.vectorField, k = 2, numCandidates = 5, queryVector = Chunk(1.0, 2.0, 3.0)))
```

If you want to create `kNN Search` request with `MultiIndex`, do the following:
```scala
val requestWithMultiIndex: KNNRequest =
  knnSearch(
    selectors = MultiIndex.names(IndexName("index1"), IndexName("index2")),
    query = kNN(field = Document.vectorField, k = 2, numCandidates = 5, queryVector = Chunk(1.0, 2.0, 3.0))
  )
```

The result of executing a `kNN Search` request is `KNNSearchResult`, from which you can get the documents (or items) of the nearest neighbors:
```scala
import zio._

val result: RIO[Elasticsearch, Chunk[Document]] =
  Elasticsearch.execute(request).documentAs[Document]
```

You can find more information about `kNN Search` request [here](https://www.elastic.co/guide/en/elasticsearch/reference/8.6/knn-search-api.html).
