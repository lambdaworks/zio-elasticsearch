---
id: elastic_query_nested
title: "Nested Query"
---

The Nested query searches nested field objects as if they were indexed as separate documents. If an object matches the search, the Nested query returns the root parent document.

In order to use the `Nested` query import following:
```scala
import zio.elasticsearch.query.NestedQuery
import zio.elasticsearch.ElasticQuery._
```

To create a type-safe instance of `NestedQuery`:
```scala
val query: NestedQuery = nested(path = Document.subDocumentList, query = matchAll)
```

To create an instance of `NestedQuery`:
```scala
val query: NestedQuery = nested(path = "testField", query = matchAll)
```

If you want to change the `ignore_unmapped`, you can use `ignoreUnmapped` method:
```scala
val queryWithIgnoreUnmapped: NestedQuery = nested(path = Document.subDocumentList, query = matchAll).ignoreUnmapped(true)
```

If you want to change the `inner_hits`, you can use `innerHits` method:
```scala
import zio.elasticsearch.query.InnerHits

val queryWithIgnoreUnmapped: NestedQuery = nested(path = Document.subDocumentList, query = matchAll).innerHits(innerHits = InnerHits.from(5))
```

If you want to change the `score_mode`, you can use `scoreMode` method:
```scala
import zio.elasticsearch.query.ScoreMode

val queryWithIgnoreUnmapped: NestedQuery = nested(path = Document.subDocumentList, query = matchAll).scoreMode(ScoreMode.Avg)
```

You can find more information about Nested query [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/query-dsl-nested-query.html).
