---
id: elastic_interval_query
title: "Overview"
---
The `Intervals` query allows for advanced search queries based on intervals between words in specific fields.
This query provides flexibility for conditions.

To use the `Intervals` query, import the following:
```scala
import zio.elasticsearch.query.IntervalQuery
import zio.elasticsearch.ElasticQuery._
```

You can create a basic `Intervals` query` using the `intervals` method:
```scala
val query: IntervalQuery[Any] = intervals(field = "content", rule = intervalMatch("targetWord"))
```

If you want to specify which fields should be searched, you can use the `useField` method:
```scala
val queryWithField: IntervalQuery[Document] =
  intervals(field = "content", rule = intervalMatch("targetWord").useField(Document.stringField))
```

To define `field` in a type-safe manner, use the overloaded `useField` method with field definitions from your document:
```scala
val queryWithSafeField: IntervalQuery[Document] =
  intervals(field = Document.stringField, rule = intervalMatch("targetWord"))
```

Alternatively, you can pass a `Field` object directly:
```scala
val queryWithFieldObject: IntervalQuery[Document] =
  intervals(field = "content", rule = intervalMatch("targetWord").useField(Document.stringField))
```

If you want to define the `maxGaps` parameter, use the `maxGaps` method:
```scala
val queryWithMaxGaps: IntervalQuery[Document] =
  intervals(field = "content", rule = intervalMatch("targetWord").maxGaps(2))
```

If you want to specify the word order requirement, use the `orderedOn` method:
```scala
val queryWithOrder: IntervalQuery[Document] =
  intervals(field = "content", rule = intervalMatch("targetWord").orderedOn())
```

You can also apply additional filters to the query:
```scala
val queryWithFilter: IntervalQuery[Document] =
  intervals(field = "content", rule = intervalMatch("targetWord").filter(IntervalFilter.someFilter))
```

Alternatively, you can construct the query manually with all parameters:
```scala
val queryManually: IntervalQuery[Document] =
  IntervalQuery(
    field = "content",
    rule = intervalMatch("targetWord")
      .maxGaps(2)
      .orderedOn()
      .filter(IntervalFilter.someFilter)
      .analyzer("standard")
  )
```

