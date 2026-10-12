---
id: elastic_query_simple_query_string
title: "Simple Query String Query"
---

The `SimpleQueryString` query provides a simple query syntax for performing searches across multiple fields.

To use the `SimpleQueryString` query, import the following:
```scala
import zio.elasticsearch.query.{MinimumShouldMatch, SimpleQueryStringQuery}
import zio.elasticsearch.ElasticQuery._
```

You can create a `SimpleQueryString` query without specifying `fields` using the `simpleQueryString` method:
```scala
val query: SimpleQueryStringQuery[Any] = simpleQueryString(query = "name")
```

If you want to specify which fields should be searched, you can use the `fields` method:
```scala
val query: SimpleQueryStringQuery[Document] =
simpleQueryString(query = "name").fields("stringField1", "stringField2")
```

To define `fields` in a type-safe manner, use the overloaded `fields` method with `field` definitions from your document:
```scala
val query: SimpleQueryStringQuery[Document] =
simpleQueryString(query = "name").fields(Document.stringField1, Document.stringField2)
```

Alternatively, you can pass a Chunk of `fields`:
```scala
val query: SimpleQueryStringQuery[Document] =
simpleQueryString(query = "name").fields(Chunk(Document.stringField1, Document.stringField2))
```

If you want to define the `minimum_should_match` parameter, use the `minimumShouldMatch` method:
```scala
val query: SimpleQueryStringQuery[Any] =
simpleQueryString(query = "name").minimumShouldMatch(2)
```

The `minimumShouldMatch` method also accepts a `MinimumShouldMatch` value, which supports percentages and conditional combinations as well (e.g. `MinimumShouldMatch.Percentage(75)` or `MinimumShouldMatch.Combination(MinimumShouldMatch.Condition(3, MinimumShouldMatch.Percentage(90)))`). You can find more information about the `minimum_should_match` parameter [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/query-dsl-minimum-should-match.html).

You can also construct the query manually with all parameters:
```scala
val query: SimpleQueryStringQuery[Document] =
SimpleQueryString(
  query = "name",
  fields = Chunk("stringField"),
  minimumShouldMatch = Some(MinimumShouldMatch.Count(2))
)
```

When a `SimpleQueryString` query is used inside a [`Nested`](https://lambdaworks.github.io/zio-elasticsearch/overview/queries/elastic_query_nested) query, the nested path is prepended to every entry in `fields`, so you should pass field names relative to the nested object:
```scala
val query: NestedQuery[Document] =
  nested(path = Document.subDocumentList, query = simpleQueryString(query = "apple").fields(SubDocument.name))
// "fields": ["subDocumentList.name"]
```

:::caution
Earlier versions ignored the nested path for `SimpleQueryString`, so the only way to use it inside `nested` was to pass the full path yourself (e.g. `fields("subDocumentList.name")`). Such field names now get the path twice (`subDocumentList.subDocumentList.name`) and the query silently matches nothing, so drop the path prefix when upgrading.
:::