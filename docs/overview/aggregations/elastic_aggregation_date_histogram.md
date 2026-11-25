---
id: elastic_aggregation_date_histogram
title: "Date Histogram Aggregation"
---

The `DateHistogram` aggregation is a multi-bucket aggregation that groups documents into buckets by date, where each bucket covers one interval (e.g. one month).

In order to use the `DateHistogram` aggregation import the following:
```scala
import zio.elasticsearch.aggregation.{CalendarInterval, DateHistogramAggregation, FixedInterval}
import zio.elasticsearch.ElasticAggregation.dateHistogramAggregation
```

The interval is either a `CalendarInterval` or a `FixedInterval`.
Calendar intervals (`Minute`, `Hour`, `Day`, `Week`, `Month`, `Quarter` and `Year`) take into account that months have different lengths and that days can be affected by daylight saving time.
Fixed intervals are a multiple of a fixed time unit (milliseconds, seconds, minutes, hours or days):
```scala
val aggregation: DateHistogramAggregation = dateHistogramAggregation(name = "dateHistogramAggregation", field = "dateField", interval = CalendarInterval.Month)

val aggregationWithFixedInterval: DateHistogramAggregation = dateHistogramAggregation(name = "dateHistogramAggregation", field = "dateField", interval = FixedInterval.days(30))
```

You can create a [type-safe](https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema) `DateHistogram` aggregation using the `dateHistogramAggregation` method this way:
```scala
// Document.dateField must be a date or a number (of milliseconds since the epoch)
val aggregation: DateHistogramAggregation = dateHistogramAggregation(name = "dateHistogramAggregation", field = Document.dateField, interval = CalendarInterval.Month)
```

If you want to compute the buckets in a time zone, you can use `timeZone` method, and if you want to shift the start of each bucket, you can use `offset` method:
```scala
import java.time.{Duration, ZoneId}

val aggregationWithTimeZone: DateHistogramAggregation = dateHistogramAggregation(name = "dateHistogramAggregation", field = Document.dateField, interval = CalendarInterval.Day).timeZone(ZoneId.of("Europe/Belgrade"))
val aggregationWithOffset: DateHistogramAggregation = dateHistogramAggregation(name = "dateHistogramAggregation", field = Document.dateField, interval = CalendarInterval.Day).offset(Duration.ofHours(6))
```

If you want to change how the keys of the buckets are formatted (in `keyAsString`), you can use `format` method:
```scala
val aggregationWithFormat: DateHistogramAggregation = dateHistogramAggregation(name = "dateHistogramAggregation", field = Document.dateField, interval = CalendarInterval.Month).format("yyyy-MM")
```

By default, empty buckets between the first and the last bucket are returned. If you want to return only buckets with a minimum number of documents, you can use `minDocCount` method:
```scala
val aggregationWithMinDocCount: DateHistogramAggregation = dateHistogramAggregation(name = "dateHistogramAggregation", field = Document.dateField, interval = CalendarInterval.Month).minDocCount(1)
```

If you want to return buckets for a whole range, even if they are empty, you can use `extendedBounds` method, and if you want to limit the buckets to a range, you can use `hardBounds` method.
Bounds are dates or numbers of milliseconds since the epoch, and either of them can be omitted:
```scala
import zio.elasticsearch.aggregation.DateHistogramBounds

val aggregationWithExtendedBounds: DateHistogramAggregation =
  dateHistogramAggregation(name = "dateHistogramAggregation", field = Document.dateField, interval = CalendarInterval.Month)
    .extendedBounds(DateHistogramBounds().min(LocalDate.of(2024, 1, 1)).max(LocalDate.of(2024, 12, 31)))

val aggregationWithHardBounds: DateHistogramAggregation =
  dateHistogramAggregation(name = "dateHistogramAggregation", field = Document.dateField, interval = CalendarInterval.Month)
    .hardBounds(DateHistogramBounds().min(LocalDate.of(2024, 1, 1)))
```

If you want to change the `order`, you can use `orderBy`, `orderByCountAsc`, `orderByCountDesc`, `orderByKeyAsc` or `orderByKeyDesc` method:
```scala
val aggregationWithOrder: DateHistogramAggregation = dateHistogramAggregation(name = "dateHistogramAggregation", field = Document.dateField, interval = CalendarInterval.Month).orderByKeyDesc
```

If you want to associate each bucket with its formatted key, you can use `keyed` method:
```scala
val aggregationWithKeyed: DateHistogramAggregation = dateHistogramAggregation(name = "dateHistogramAggregation", field = Document.dateField, interval = CalendarInterval.Month).keyed
```

If you want to put documents without a value in a bucket, you can use `missing` method:
```scala
val aggregationWithMissing: DateHistogramAggregation = dateHistogramAggregation(name = "dateHistogramAggregation", field = Document.dateField, interval = CalendarInterval.Month).missing(LocalDate.of(2000, 1, 1))
```

If you want to add aggregation (on the same level), you can use `withAgg` method:
```scala
val multipleAggregations: MultipleAggregations = dateHistogramAggregation(name = "dateHistogramAggregation", field = Document.dateField, interval = CalendarInterval.Month).withAgg(maxAggregation(name = "maxAggregation", field = Document.intField))
```

If you want to add another sub-aggregation, you can use `withSubAgg` method. Parent pipeline aggregations, such as `cumulativeSum` and `derivative`, can be used as sub-aggregations:
```scala
val aggregationWithSubAgg: DateHistogramAggregation =
  dateHistogramAggregation(name = "dateHistogramAggregation", field = Document.dateField, interval = CalendarInterval.Month)
    .withSubAgg(sumAggregation(name = "sumAggregation", field = Document.intField))
    .withSubAgg(cumulativeSumAggregation(name = "cumulativeSumAggregation", bucketsPath = "sumAggregation"))
```

If you want to add custom metadata, which Elasticsearch returns together with the aggregation result, you can use `meta` method:
```scala
val aggregationWithMeta: DateHistogramAggregation = dateHistogramAggregation(name = "dateHistogramAggregation", field = Document.dateField, interval = CalendarInterval.Month).meta("color", "blue")
```

The result is retrieved with the `asDateHistogramAggregation` accessor. Each of its buckets has a `key` (the start of the bucket in milliseconds since the epoch), a `keyAsString` (the formatted start of the bucket) and a `docCount`.

You can find more information about `DateHistogram` aggregation [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/search-aggregations-bucket-datehistogram-aggregation.html).
