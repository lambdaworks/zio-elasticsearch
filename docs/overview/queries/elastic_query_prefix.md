---
id: elastic_query_prefix
title: "Prefix Query"
---

The `Prefix` query returns documents that contain a specific prefix in a provided field.

In order to use the `Prefix` query import the following:
```scala
import zio.elasticsearch.query.PrefixQuery
import zio.elasticsearch.ElasticQuery._
```

You can create a `Prefix` query using the `prefix` method this way:
```scala
val query: PrefixQuery = prefix(field = Document.name, value = "test")
```

You can create a [type-safe](https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema) `Prefix` query using the `prefix` method this way:
```scala
val query: PrefixQuery = prefix(field = Document.name, value = "test")
```

If you want to change the `case_insensitive`, you can use `caseInsensitive`, `caseInsensitiveFalse` or `caseInsensitiveTrue` method:
```scala
val queryWithCaseInsensitive: PrefixQuery = prefix(field = Document.name, value = "test").caseInsensitive(true)
val queryWithCaseInsensitiveFalse: PrefixQuery = prefix(field = Document.name, value = "test").caseInsensitiveFalse
val queryWithCaseInsensitiveTrue: PrefixQuery = prefix(field = Document.name, value = "test").caseInsensitiveTrue
```

You can find more information about `Prefix` query [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/query-dsl-prefix-query.html).
