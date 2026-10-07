---
id: overview_elastic_sort
title: "Sorting"
---

Sorting allows you to add one or more sorts on specific fields (or on a script) to `Search` and `SearchAndAggregate` requests, as well as to the `BucketSort` aggregation.
Sorts are described with the `Sort` data type, which can be constructed from the DSL methods found under the following import:
```scala
import zio.elasticsearch.ElasticSort._
```

## Sort by field

In order to sort by field import the following:
```scala
import zio.elasticsearch.query.sort.SortByField
import zio.elasticsearch.ElasticSort.sortBy
```

You can create a `SortByField` using the `sortBy` method this way:
```scala
val sort: SortByField = sortBy(field = "intField")
```

You can create a [type-safe](https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema) `SortByField` using the `sortBy` method this way:
```scala
val sort: SortByField = sortBy(field = Document.intField)
```

There are also predefined sorts by special fields, which can be found in the `SortByField` object:
```scala
import zio.elasticsearch.query.sort.SortByField.{byCount, byDoc, byKey, byScore}

// sort search results by the `_doc` field
val sortByDoc: SortByField = byDoc
// sort search results by the `_score` field
val sortByScore: SortByField = byScore
// sort buckets by the `_count` field (in the context of an aggregation)
val sortByCount: SortByField = byCount
// sort buckets by the `_key` field (in the context of an aggregation)
val sortByKey: SortByField = byKey
```

If you want to change the `format`, you can use `format` method:
```scala
val sortWithFormat: SortByField = sortBy(field = Document.dateField).format("strict_date_optional_time_nanos")
```

If you want to change the `missing`, you can use `missing` method:
```scala
import zio.elasticsearch.query.sort.Missing._

val sortWithMissingFirst: SortByField = sortBy(field = Document.intField).missing(First)
val sortWithMissingLast: SortByField = sortBy(field = Document.intField).missing(Last)
```

If you want to change the `mode`, you can use `mode` method:
```scala
import zio.elasticsearch.query.sort.SortMode._

val sortWithModeAvg: SortByField = sortBy(field = Document.intListField).mode(Avg)
val sortWithModeMax: SortByField = sortBy(field = Document.intListField).mode(Max)
val sortWithModeMedian: SortByField = sortBy(field = Document.intListField).mode(Median)
val sortWithModeMin: SortByField = sortBy(field = Document.intListField).mode(Min)
val sortWithModeSum: SortByField = sortBy(field = Document.intListField).mode(Sum)
```

If you want to change the `numeric_type`, you can use `numericType` method:
```scala
import zio.elasticsearch.query.sort.NumericType

val sortWithNumericTypeDouble: SortByField = sortBy(field = Document.intField).numericType(NumericType.Double)
val sortWithNumericTypeLong: SortByField = sortBy(field = Document.intField).numericType(NumericType.Long)
val sortWithNumericTypeDate: SortByField = sortBy(field = Document.dateField).numericType(NumericType.Date)
val sortWithNumericTypeDateNanos: SortByField = sortBy(field = Document.dateField).numericType(NumericType.DateNanos)
```

If you want to change the `order`, you can use `order` method:
```scala
import zio.elasticsearch.query.sort.SortOrder._

val sortWithOrderAsc: SortByField = sortBy(field = Document.intField).order(Asc)
val sortWithOrderDesc: SortByField = sortBy(field = Document.intField).order(Desc)
```

If you want to change the `unmapped_type`, you can use `unmappedType` method:
```scala
val sortWithUnmappedType: SortByField = sortBy(field = Document.intField).unmappedType("long")
```

You can also combine all of the parameters above:
```scala
val sortWithAllParams: SortByField =
  sortBy(field = Document.dateField)
    .format("strict_date_optional_time_nanos")
    .missing(First)
    .mode(Avg)
    .numericType(NumericType.Long)
    .order(Desc)
    .unmappedType("long")
```

## Sort by script

In order to sort by script import the following:
```scala
import zio.elasticsearch.query.sort.SortByScript
import zio.elasticsearch.query.sort.SourceType._
import zio.elasticsearch.ElasticSort.sortBy
import zio.elasticsearch.script.Script
```

You can create a `SortByScript` using the `sortBy` method with a `Script` and the type of the values the script returns (`NumberType` or `StringType`):
```scala
val sort: SortByScript = sortBy(script = Script("doc['intField'].value"), sourceType = NumberType)
val sortWithParams: SortByScript =
  sortBy(script = Script("doc['intField'].value * params['factor']").params("factor" -> 2), sourceType = NumberType)
```

If you want to change the `mode`, you can use `mode` method:
```scala
import zio.elasticsearch.query.sort.SortMode.Avg

val sortWithMode: SortByScript = sortBy(script = Script("doc['intField'].value"), sourceType = NumberType).mode(Avg)
```

If you want to change the `order`, you can use `order` method:
```scala
import zio.elasticsearch.query.sort.SortOrder.Desc

val sortWithOrder: SortByScript = sortBy(script = Script("doc['intField'].value"), sourceType = NumberType).order(Desc)
```

## Using sorts

Sorts can be added to the [`Search` and `SearchAndAggregate`](https://lambdaworks.github.io/zio-elasticsearch/overview/requests/elastic_request_search) requests using the `sort` method, which accepts one or more sorts:
```scala
import zio.elasticsearch._
import zio.elasticsearch.ElasticQuery.matchAll
import zio.elasticsearch.ElasticRequest.{search, SearchRequest}
import zio.elasticsearch.query.sort.SortByField.byScore
import zio.elasticsearch.query.sort.SortOrder.{Asc, Desc}

val request: SearchRequest =
  search(selectors = IndexName("index"), query = matchAll)
    .sort(sortBy(Document.intField).order(Desc), sortBy("stringField").order(Asc), byScore)
```

Sorts can also be used in the [`BucketSort`](https://lambdaworks.github.io/zio-elasticsearch/overview/aggregations/elastic_aggregation_bucket_sort) aggregation:
```scala
import zio.elasticsearch.ElasticAggregation.bucketSortAggregation
import zio.elasticsearch.aggregation.BucketSortAggregation
import zio.elasticsearch.query.sort.SortByField.{byCount, byKey}

val aggregation: BucketSortAggregation = bucketSortAggregation(name = "aggregationSort").sort(byCount, byKey)
```

You can find more information about sorting [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/sort-search-results.html).
