The `SimpleQueryString` query provides a simple query syntax for performing searches across multiple fields.

To use the `SimpleQueryString` query, import the following:
```scala
import zio.elasticsearch.query.SimpleQueryStringQuery
import zio.elasticsearch.ElasticQuery._
```

You can create a `SimpleQueryString` query without specifying `fields` using the `simpleQueryString` method:
```scala
val query: SimpleQueryStringQuery[Any] = simpleQueryString("name")
```

If you want to specify which `fields` should be searched, you can use the `fields` method:
```scala
val query: SimpleQueryStringQuery[Any] =
simpleQueryString("name").fields("stringField1", "stringField2")
```

To define `fields` in a type-safe manner, use the overloaded `fields` method with `field` definitions from your document:
```scala
val query: SimpleQueryStringQuery[Document] =
simpleQueryString("name").fields(Document.stringField1, Document.stringField2)
```

Alternatively, you can pass a Chunk of `fields`:
```scala
val query: SimpleQueryStringQuery[Document] =
simpleQueryString("name").fields(Chunk(Document.stringField1, Document.stringField2))
```

If you want to define the `minimum_should_match` parameter, use the `minimumShouldMatch` method:
```scala
val query: SimpleQueryStringQuery[Any] =
simpleQueryString("name").minimumShouldMatch(2)
```

You can also construct the query manually with all parameters:
```scala
val query: SimpleQueryStringQuery[Document] =
SimpleQueryString(
  query = "test",
  fields = Chunk("stringField"),
  minimumShouldMatch = Some(2)
)
```