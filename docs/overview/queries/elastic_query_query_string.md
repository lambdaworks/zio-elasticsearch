---
id: elastic_query_query_string
title: "Query String Query"
---

The `QueryString` query returns documents based on a provided query string, using a parser with a strict syntax.

To use the `QueryString` query, import the following:
```scala
import zio.elasticsearch.query.QueryStringQuery
import zio.elasticsearch.ElasticQuery._
```

You can create a `QueryString` query without specifying `fields` using the `queryString` method:
```scala
val query: QueryStringQuery[Any] = queryString(query = "name")
```

If you want to specify which fields should be searched, you can use the `fields` method:
```scala
val query: QueryStringQuery[Any] =
queryString(query = "name").fields("stringField1", "stringField2")
```

To define `fields` in a type-safe manner, use the overloaded `fields` method with `field` definitions from your document:
```scala
val query: QueryStringQuery[Document] =
queryString(query = "name").fields(Document.stringField1, Document.stringField2)
```

Alternatively, you can pass a `Chunk` of `fields`:
```scala
val query: QueryStringQuery[Document] =
queryString(query = "name").fields(Chunk(Document.stringField1, Document.stringField2))
```

You can also specify type-safe `fields` when constructing the query itself:
```scala
val query: QueryStringQuery[Document] =
queryString(query = "name", Document.stringField1, Document.stringField2)
```

If you want to define the `boost` parameter, use the `boost` method:
```scala
val query: QueryStringQuery[Any] =
queryString(query = "name").boost(2.0)
```

If you want to define the `minimum_should_match` parameter, use the `minimumShouldMatch` method:
```scala
val query: QueryStringQuery[Any] =
queryString(query = "name").minimumShouldMatch(2)
```

If you want to define the `default_field` parameter, use the `defaultField` method:
```scala
val query: QueryStringQuery[Any] =
queryString(query = "name").defaultField("stringField")
```

You can combine all parameters:
```scala
val query: QueryStringQuery[Document] =
queryString(query = "name", Document.stringField1, Document.stringField2)
  .defaultField("stringField1")
  .boost(2.0)
  .minimumShouldMatch(2)
```
