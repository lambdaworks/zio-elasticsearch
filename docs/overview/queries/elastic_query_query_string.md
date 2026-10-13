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

The `minimumShouldMatch` method also accepts a `MinimumShouldMatch` value, which supports percentages and conditional combinations as well (e.g. `MinimumShouldMatch.Percentage(75)` or `MinimumShouldMatch.Combination(MinimumShouldMatch.Condition(3, MinimumShouldMatch.Percentage(90)))`). You can find more information about the `minimum_should_match` parameter [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/query-dsl-minimum-should-match.html).

If you want to define the `default_field` parameter, use the `defaultField` method:
```scala
val query: QueryStringQuery[Any] =
queryString(query = "name").defaultField("stringField")
```

Elasticsearch does not allow `default_field` and `fields` to be used together, so they are mutually exclusive: calling `defaultField` clears any previously set `fields`, and calling `fields` clears any previously set `default_field`.

You can combine `fields` (or `default_field`) with the other parameters:
```scala
val query: QueryStringQuery[Document] =
queryString(query = "name", Document.stringField1, Document.stringField2)
  .boost(2.0)
  .minimumShouldMatch(2)
```

When a `QueryString` query is used inside a [`Nested`](https://lambdaworks.github.io/zio-elasticsearch/overview/queries/elastic_query_nested) query, the nested path is prepended to every entry in `fields` and to `default_field`, so you should pass field names relative to the nested object:
```scala
val query: NestedQuery[Document] =
  nested(path = Document.subDocumentList, query = queryString(query = "apple").fields(SubDocument.name))
// "fields": ["subDocumentList.name"]
```

:::caution
Earlier versions ignored the nested path for `QueryString`, so the only way to use it inside `nested` was to pass the full path yourself (e.g. `fields("subDocumentList.name")`). Such field names now get the path twice (`subDocumentList.subDocumentList.name`) and the query silently matches nothing, so drop the path prefix when upgrading.
:::

Field names written in the query text itself (e.g. `name:apple AND color:red`) are sent as they are. Inside a `Nested` query, use the full path for them (e.g. `subDocumentList.name:apple`).
