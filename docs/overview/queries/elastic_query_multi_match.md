---
id: elastic_query_multi_match
title: "Multi Match Query"
---

The `MultiMatch` query builds on the `match` query to allow multi-field queries:

In order to use the `MultiMatch` query import the following:
```scala
import zio.elasticsearch.query.MultiMatchQuery
import zio.elasticsearch.ElasticQuery._
```

You can create a `MultiMatch` query without specifying fields using the `multiMatch` method this way:
```scala
val query: MultiMatchQuery = multiMatch(value = "test")
```

You can create a `MultiMatch` query with fields that will be searched using the `multiMatch` method this way:
```scala
val query: MultiMatchQuery = multiMatch(value = "test").fields("stringField1","stringField2")
```

You can create a [type-safe](https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema) `MultiMatch` query with fields that will be searched using the `multiMatch` method this way:
```scala
val query: MultiMatchQuery = multiMatch(value = "test").fields(Document.stringField1, Document.stringField2)
```

You can find more information about `MultiMatch` query [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/query-dsl-multi-match-query.html#query-dsl-multi-match-query).

