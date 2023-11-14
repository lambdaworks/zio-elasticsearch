---
id: elastic_query_boost
title: "Boosting Query"
---

The `Boosting` query returns documents that match the positive query. Among those documents, the ones that also match the negative query get their relevance score lowered by multiplying it by the negative boosting factor. 

In order to use the `Boosting` query import the following:
```scala
import zio.elasticsearch.query.BoostQuery
import zio.elasticsearch.ElasticQuery.boostQuery
```

You can create a `Boosting` query using the `boost` method this way:
```scala
val query: BoostQuery = boost(negativeBoost = 0.5f, negativeQuery = contains(field = "testField", value = "a"), positiveQuery = startsWith(field = "testId", value = "b"))
```

You can create a [type-safe](https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema) `Boosting` query using the `boost` method this way:
```scala
val query: BoostQuery = boost(negativeBoost = 0.5f, negativeQuery = contains(field = Document.stringField, value = "a"), positiveQuery = startsWith(field = Document.id, value = "b"))
```

You can find more information about `Boosting` query [here](https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-boosting-query.html#boosting-query-ex-request).
