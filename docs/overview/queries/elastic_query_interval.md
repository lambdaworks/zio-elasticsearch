---
id: elastic_query_interval
title: "Interval Query"
---

The `Intervals` query allows advanced search based on the relative order and proximity of matching terms within a
field.

To use the `Intervals` query, import the following:
```scala
import zio.elasticsearch.query.{IntervalRule, IntervalsQuery}
import zio.elasticsearch.ElasticIntervalRule._
import zio.elasticsearch.ElasticQuery._
```

You can create an `Intervals` query by combining a field with an interval rule, such as `intervalMatch`:
```scala
val query: IntervalsQuery[Any] = intervals(field = "content", rule = intervalMatch("targetWord"))
```

You can create a [type-safe](https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema)
`Intervals` query using a field definition from your document:
```scala
val queryWithTypedField: IntervalsQuery[Document] =
  intervals(field = Document.stringField, rule = intervalMatch("targetWord"))
```

Other interval rules are available too, such as `intervalPrefix`, `intervalWildcard`, `intervalFuzzy`, `intervalRange`,
`intervalRegexp`, `intervalAllOf` and `intervalAnyOf`:
```scala
val queryWithPrefix: IntervalsQuery[Any]   = intervals(field = "content", rule = intervalPrefix("tar"))
val queryWithWildcard: IntervalsQuery[Any] = intervals(field = "content", rule = intervalWildcard("t?rget*"))
val queryWithRange: IntervalsQuery[Any]    = intervals(field = "content", rule = intervalRange.gte("apple").lt("banana"))
val queryWithRegexp: IntervalsQuery[Any]   = intervals(field = "content", rule = intervalRegexp("t.*get"))
val queryWithFuzzy: IntervalsQuery[Any]    = intervals(field = "content", rule = intervalFuzzy("target").fuzziness("AUTO"))
```

If you want to require the matching terms to appear in the order specified, use the `orderedOn` method:
```scala
val queryWithOrder: IntervalsQuery[Any] = intervals(field = "content", rule = intervalMatch("targetWord").orderedOn)
```

If you want to limit the maximum number of positions allowed between the matching terms, use the `maxGaps` method:
```scala
val queryWithMaxGaps: IntervalsQuery[Any] = intervals(field = "content", rule = intervalMatch("targetWord").maxGaps(2))
```

If the terms for a rule should be extracted from a different field than the one the `Intervals` query targets, use the
`useField` method:
```scala
val queryWithRuleField: IntervalsQuery[Any] =
  intervals(field = "content", rule = intervalMatch("targetWord").useField("otherField"))
```

You can also use the type-safe `useField` variant, which narrows the rule to the field's document type. A type-safe
`Intervals` query only accepts rules defined for the same document type as its field, so mixing fields from different
documents is a compile error:
```scala
val queryWithTypedRuleField: IntervalsQuery[Document] =
  intervals(field = Document.stringField, rule = intervalMatch("targetWord").useField(Document.otherField))
```

You can also restrict matches using another interval rule with the `filter` method, for example to exclude documents
where the matched interval is immediately followed by another term:
```scala
val queryWithFilter: IntervalsQuery[Any] =
  intervals(
    field = "content",
    rule = intervalMatch("targetWord").filter(intervalFilter(notContaining = Some(intervalMatch("excludedWord"))))
  )
```

You can find more information about the `Intervals` query
[here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/query-dsl-intervals-query.html).
