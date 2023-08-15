---
id: elastic_query_regexp
title: "Regexp Query"
---

The `Regexp` query returns documents that contain terms matching a regular expression.

In order to use the `Regexp` query import the following:
```scala
import zio.elasticsearch.query.RegexpQuery
import zio.elasticsearch.ElasticQuery._
```

You can create a `Regexp` query using the `regexp` method this way:
```scala
val query: RegexpQuery = regexp(field = "name", value = "t.*st")
```

You can create a [type-safe](https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema) `Regexp` query using the `regexp` method this way:
```scala
val query: RegexpQuery = regexp(field = Document.name, value = "t.*st")
```

If you want to change the `case_insensitive`, you can use `caseInsensitive`, `caseInsensitiveFalse` or `caseInsensitiveTrue` method:
```scala
val queryWithCaseInsensitive: RegexpQuery = regexp(field = Document.name, value = "t.*st").caseInsensitive(true)
val queryWithCaseInsensitiveFalse: RegexpQuery = regexp(field = Document.name, value = "t.*st").caseInsensitiveFalse
val queryWithCaseInsensitiveTrue: RegexpQuery = regexp(field = Document.name, value = "t.*st").caseInsensitiveTrue
```

You can find more information about `Regexp` query [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/query-dsl-regexp-query.html).

