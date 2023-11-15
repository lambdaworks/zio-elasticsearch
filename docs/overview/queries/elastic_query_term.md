---
id: elastic_query_term
title: "Term Query"
---

The `Term` query returns documents that contain an exact term in the provided field.

In order to use the `Term` query import the following:
```scala
import zio.elasticsearch.query.TermQuery
import zio.elasticsearch.ElasticQuery._
```

You can create a `Term` query using the `term` method this way:
```scala
val query: TermQuery = term(field = "stringField", value = "test")
```

You can create a [type-safe](https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema) `Term` query using the `term` method this way:
```scala
val query: TermQuery = term(field = Document.name, value = "test")
```

If you want to change the `boost`, you can use `boost` method:
```scala
val queryWithBoost: TermQuery = term(field = Document.name, value = "test").boost(2.0)
```

If you want to change the `case_insensitive`, you can use `caseInsensitive`, `caseInsensitiveFalse` or `caseInsensitiveTrue` method:
```scala
val queryWithCaseInsensitive: TermQuery = term(field = Document.name, value = "test").caseInsensitive(true)
val queryWithCaseInsensitiveFalse: TermQuery = term(field = Document.name, value = "test").caseInsensitiveFalse
val queryWithCaseInsensitiveTrue: TermQuery = term(field = Document.name, value = "test").caseInsensitiveTrue
```

You can find more information about `Term` query [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/query-dsl-term-query.html).
