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

If you want to change the `fields` that will be searched, you can use the `fields` method:
```scala
val query: MultiMatchQuery = multiMatch(value = "test").fields("stringField1", "stringField2")
```

If you want to change the [type-safe](https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema) `fields` that will be searched, you can use the `fields` method this way:
```scala
val query: MultiMatchQuery = multiMatch(value = "test").fields(Document.stringField1, Document.stringField2)
```

If you want to change the `type` of `MultiMatch` query, you can use the `matchingType` method:
```scala
import zio.elasticsearch.query.MultiMatchType

val query: MultiMatchQuery = multiMatch(value = "test").fields(Document.stringField1, Document.stringField2).matchingType(MultiMatchType.MostFields)
```

If you want to change the `boost`, you can use the `boost` method:
```scala
val query: MultiMatchQuery = multiMatch(value = "test").fields(Document.stringField1, Document.stringField2).boost(2.2)
```

If you want to change the `minimum_should_match`, you can use the `minimumShouldMatch` method:
```scala
val query: MultiMatchQuery = multiMatch(value = "test").fields(Document.stringField1, Document.stringField2).minimumShouldMatch(2)
```

You can find more information about `MultiMatch` query [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/query-dsl-multi-match-query.html#query-dsl-multi-match-query).

