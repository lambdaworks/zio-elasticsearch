---
id: elastic_aggregation_ip_range
title: "IP Range Aggregation"
---

The `IP Range` aggregation is a multi-bucket aggregation that creates buckets for ranges of IP addresses, either using `from`/`to` values or `CIDR` masks.

In order to use the `IP Range` aggregation import the following:
```scala
import zio.elasticsearch.aggregation.{IpRangeAggregation, IpRangeBound}
import zio.elasticsearch.ElasticAggregation.ipRangeAggregation
```

You can create a [type-safe](https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema) `IpRangeAggregation` using the `ipRangeAggregation` method this way:
```scala
val aggregation: IpRangeAggregation =
  ipRangeAggregation(
    name = "ipRangeAggregation",
    field = Document.ipField,
    range = IpRangeBound(to = Some("10.0.0.5")),
    ranges = IpRangeBound(from = Some("10.0.0.5"))
  )
```

You can create an `IpRangeAggregation` using the `ipRangeAggregation` method this way:
```scala
val aggregation: IpRangeAggregation =
  ipRangeAggregation(
    name = "ipRangeAggregation",
    field = "ipField",
    range = IpRangeBound(to = Some("10.0.0.5")),
    ranges = IpRangeBound(from = Some("10.0.0.5"))
  )
```

You can also use `CIDR` masks for ranges:
```scala
val aggregation: IpRangeAggregation =
  ipRangeAggregation(
    name = "ipRangeAggregation",
    field = "ipField",
    range = IpRangeBound(mask = Some("10.0.0.0/25")),
    ranges = IpRangeBound(mask = Some("10.0.0.128/25"))
  )
```

If you want to associate each bucket with a unique string key, you can use the `keyed` method together with the `key` of each range:
```scala
val aggregationWithKeyed: IpRangeAggregation =
  ipRangeAggregation(
    name = "ipRangeAggregation",
    field = "ipField",
    range = IpRangeBound(mask = Some("10.0.0.0/25")).key("low"),
    ranges = IpRangeBound(mask = Some("10.0.0.128/25")).key("high")
  ).keyed
```

If you want to add aggregation (on the same level), you can use `withAgg` method:
```scala
val multipleAggregations: MultipleAggregations =
  ipRangeAggregation(name = "ipRangeAggregation", field = "ipField", range = IpRangeBound(to = Some("10.0.0.5")))
    .keyed
    .withAgg(maxAggregation(name = "maxAggregation", field = "intField"))
```

If you want to add another sub-aggregation, you can use `withSubAgg` method:
```scala
val aggregationWithSubAgg: IpRangeAggregation =
  ipRangeAggregation(name = "ipRangeAggregation", field = "ipField", range = IpRangeBound(to = Some("10.0.0.5")))
    .withSubAgg(maxAggregation(name = "maxAggregation", field = "intField"))
```

You can find more information about `IP Range` aggregation [here](https://www.elastic.co/docs/reference/aggregations/search-aggregations-bucket-iprange-aggregation).
