---
id: elastic_query_terms
title: "Terms Query"
---

The Terms query returns documents that contain one or more exact terms in a provided field.
The Terms query is the same as the Term query, except you can search for multiple values.

In order to use the `TermsQuery` query import following:
```scala
import zio.elasticsearch.query.TermsQuery
import zio.elasticsearch.ElasticQuery._
```

To create a type-safe instance of `TermsQuery`:
```scala
val query: TermQuery = terms(field = Document.name, "a", "b", "c")
```

To create an instance of `TermsQuery`:
```scala
val query: TermsQuery = terms(field = "name", "a", "b", "c")
```

If you want to change the `boost`, you can use `boost` method:
```scala
val queryWithBoost: TermsQuery = terms(field = "name", "a", "b", "c").boost(2.0)
```

You can find more information about Term Query [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/query-dsl-terms-query.html).
