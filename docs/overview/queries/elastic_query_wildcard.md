---
id: elastic_query_wildcard
title: "Wildcard Query"
---

The `Wildcard` query returns documents that contain terms matching a wildcard pattern. You can combine wildcard operators with other characters to create a wildcard pattern.

In order to use the `Wildcard` query import the following:
```scala
import zio.elasticsearch.query.WildcardQuery
import zio.elasticsearch.ElasticQuery._
```

The `Wildcard` query can be created with `contains`, `startsWith` or `wildcard` method. 
The `contains` method is adjusted `wildcard` method, that returns documents that contain terms containing provided text.
The `startsWith` method is adjusted `wildcard` method that returns documents that contain terms starting with provided text.

To create a `Wildcard` query use one of the following methods:
```scala
val query: WildcardQuery = contains(field = "name", value = "a")
val query: WildcardQuery = startsWith(field = "name", value = "a")
val query: WildcardQuery = wildcard(field = "name", value = "a*a*")
```

To create a [type-safe](https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema) `Wildcard` query use one of the following methods:
```scala
val query: WildcardQuery = contains(field = Document.name, value = "a")
val query: WildcardQuery = startsWith(field = Document.name, value = "a")
val query: WildcardQuery = wildcard(field = Document.name, value = "a*a*")
```

If you want to change the `boost`, you can use `boost` method:
```scala
val queryWithBoost: WildcardQuery = contains(field = Document.name, value = "test").boost(2.0)
```

If you want to change the `case_insensitive`, you can use `caseInsensitive`, `caseInsensitiveFalse` or `caseInsensitiveTrue`  method:
```scala
val queryWithCaseInsensitive: WildcardQuery = contains(field = Document.name, value = "a").caseInsensitive(true)
val queryWithCaseInsensitiveFalse: WildcardQuery = contains(field = Document.name, value = "a").caseInsensitiveFalse
val queryWithCaseInsensitiveTrue: WildcardQuery = contains(field = Document.name, value = "a").caseInsensitiveTrue
```

You can find more information about `Wildcard` Query [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/query-dsl-wildcard-query.html#query-dsl-wildcard-query).
