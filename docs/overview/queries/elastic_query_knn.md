---
id: elastic_query_knn
title: "kNN Query"
---

The `kNN` query finds the `k` nearest vectors to a query vector, as measured by a similarity metric. It is used for performing k-nearest neighbor (kNN) search on fields of type `dense_vector`.

Note that the `kNN` query can't be used as a regular query (e.g. in a `Search` request or within a `Bool` query); it can only be executed using the [`kNN Search`](https://lambdaworks.github.io/zio-elasticsearch/overview/requests/elastic_request_knn_search) request.

In order to use the `kNN` query import the following:
```scala
import zio.Chunk
import zio.elasticsearch.query.KNNQuery
import zio.elasticsearch.ElasticQuery._
```

You can create a `kNN` query using the `kNN` method this way:
```scala
val query: KNNQuery[Any] = kNN(field = "vectorField", k = 2, numCandidates = 5, queryVector = Chunk(1.0, 2.0, 3.0))
```

You can create a [type-safe](https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema) `kNN` query using the `kNN` method this way:
```scala
val query: KNNQuery[Document] = kNN(field = Document.vectorField, k = 2, numCandidates = 5, queryVector = Chunk(1.0, 2.0, 3.0))
```

Parameter `k` represents the number of nearest neighbors to return as top hits, while `numCandidates` represents the number of nearest neighbor candidates to consider per shard. Parameter `k` must be less than or equal to `numCandidates`.

If you want to change the `similarity`, you can use `similarity` method:
```scala
val queryWithSimilarity: KNNQuery[Document] =
  kNN(field = Document.vectorField, k = 2, numCandidates = 5, queryVector = Chunk(1.0, 2.0, 3.0)).similarity(3.0)
```

You can find more information about `kNN` query [here](https://www.elastic.co/guide/en/elasticsearch/reference/8.6/knn-search.html).
