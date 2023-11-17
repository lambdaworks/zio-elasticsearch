---
id: elastic_query_boosting
title: "Boosting Query"
---

The `Boosting` query returns documents that match the query marked as `positive` while reducing the relevance score of documents that also match a query which is marked as `negative` query.

In order to use the `Boosting` query import the following:
```scala
import zio.elasticsearch.query.BoostingQuery
import zio.elasticsearch.ElasticQuery.boostingQuery
```

You can create a `Boosting` query using the `boosting` method this way:
```scala
val query: BoostingQuery = boosting(negativeBoost = 0.5f, negativeQuery = contains(field = "testField", value = "a"), positiveQuery = startsWith(field = "testId", value = "b"))
```

You can create a [type-safe](https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema) `Boosting` query using the `boosting` method this way:
```scala
val query: BoostingQuery = boosting(negativeBoost = 0.5f, negativeQuery = contains(field = Document.stringField, value = "a"), positiveQuery = startsWith(field = Document.id, value = "b"))
```

You can find more information about `Boosting` query [here](https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-boosting-query.html#boosting-query-ex-request).
