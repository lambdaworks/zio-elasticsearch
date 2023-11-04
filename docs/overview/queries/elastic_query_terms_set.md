---
id: elastic_query_terms_set
title: "Terms Set Query"
---

The `TermsSet` query returns documents that contain the minimum amount of exact terms in a provided field. The terms set query is the same as [[zio.elasticsearch.query.TermsQuery]], except you can define the number of matching terms required to return a document.

In order to use the `TermsSet` query import the following:
```scala
import zio.elasticsearch.query.TermsSetQuery
import zio.elasticsearch.ElasticQuery.termsSetQuery
```

You can create a `TermsSet` query with defined `minimumShouldMatchField` using the `termsSet` method this way:
```scala
val query: TermsSetQuery = termsSet(field = "stringField", minimumShouldMatchField = "intField", terms = "a", "b", "c")
```

You can create a [type-safe](https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema) `TermsSet` query with defined `minimumShouldMatchField` using the `termsSet` method this way:
```scala
val query: TermsSetQuery = termsSet(field = Document.name, minimumShouldMatchField = "intField", terms = 1, 2, 3)
```

You can create a `TermsSet` query with defined `minimumShouldMatchScript` using the `termsSetScript` method this way:
```scala
val query: TermsSetQuery = termsSetScript(field = "stringField", minimumShouldMatchScript = Script("doc['intField'].value"), terms = 1, 2, 3)
```

You can create a [type-safe](https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema) `TermsSet` query with defined `minimumShouldMatchScript` using the `termsSetScript` method this way:
```scala
val query: TermsSetQuery = termsSetScript(field = Document.name, minimumShouldMatchScript = Script("doc['intField'].value"), terms = "a", "b", "c")
```

If you want to change the `boost`, you can use `boost` method:
```scala
val queryWithBoost: TermsSetQuery = termsSet(field = "booleanField", minimumShouldMatchField = "intField", terms = true, false).boost(2.0)
val queryWithBoost: TermsSetQuery = termsSetScript(field = "booleanField", minimumShouldMatchScript = Script("doc['intField'].value"), terms = true, false).boost(2.0)
```

You can find more information about `TermsSet` query [here](https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-terms-set-query.html).
