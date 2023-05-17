---
id: elastic_query_range
title: "Range Query"
---

A query that matches documents that contain terms within a provided range.

In order to use the `Range` query import following:
```scala
import zio.elasticsearch.query.RangeQuery
import zio.elasticsearch.ElasticQuery._
```

To create a type-safe instance of `RangeQuery`:
```scala
val query: RangeQuery = range(field = Document.intField)
```

To create an instance of `RangeQuery`:
```scala
val query: RangeQuery = range(field = "intField")
```

If you want to change `boost`, you can use the `boost` method:
```scala
val queryWithBoost: RangeQuery = range(field = Document.intField).boost(2.0)
```

If you want to change `format`, you can use the `format` method:
```scala
val queryWithFormat: RangeQuery = range(field = Document.dateField).format("uuuu-MM-dd")
```

If you want to change `gt` (greater than), you can use the `gt` method:
```scala
val queryWithGt: RangeQuery = range(field = Document.intField).gt(1)
```

If you want to change `gte` (greater than or equal to), you can use the `gte` method:
```scala
val queryWithGte: RangeQuery = range(field = Document.intField).gte(1)
```

If you want to change `lt` (less than), you can use the `lt` method:
```scala
val queryWithLt: RangeQuery = range(field = Document.intField).lt(100)
```

If you want to change `lte` (less than or equal to), you can use the `lte` method:
```scala
val queryWithLte: RangeQuery = range(field = Document.intField).lte(100)
```

You can find more information about Range Query [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/query-dsl-range-query.html).
