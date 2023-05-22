---
id: elastic_query_terms
title: "Terms Query"
---

The `Terms` query returns documents that contain one or more exact terms in a provided field.
This query is the same as the Term query, except you can search for multiple values.

In order to use the `TermsQuery` query import the following:
```scala
import zio.elasticsearch.query.TermsQuery
import zio.elasticsearch.ElasticQuery._
```

You can create a `Terms` query using the `terms` method this way:
```scala
val query: TermsQuery = terms(field = "name", "a", "b", "c")
```

You can create a [type-safe](https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema) `Terms` query using the `terms` method this way:
```scala
val query: TermQuery = terms(field = Document.name, "a", "b", "c")
```

If you want to change the `boost`, you can use `boost` method:
```scala
val queryWithBoost: TermsQuery = terms(field = "name", "a", "b", "c").boost(2.0)
```

You can find more information about `Terms` Query [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/query-dsl-terms-query.html).
