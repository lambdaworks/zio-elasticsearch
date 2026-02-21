---
id: elastic_aggregation_ip_range
title: "Ip Range Aggregation"
---

The `Ip Range` aggregation is a multi-bucket aggregation that creates buckets for ranges of IP addresses, either using `from`/`to` values or `CIDR` masks.

In order to use the `IP Range` aggregation import the following:
```scala
import zio.elasticsearch.aggregation.IpRangeAggregation
import zio.elasticsearch.ElasticAggregation.ipRangeAggregation
```

You can create a [type-safe](https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema) `IpRange` aggregation using the `IpRangeAggregation` method this way:
```scala
val aggregation: IpRangeAggregation = 
  ipRangeAggregation(
    name = "ip_range_agg",
    field = Document.stringField,
    ranges = NonEmptyChunk(
      IpRange.IpRangeBound(to = Some("10.0.0.5")),
      IpRange.IpRangeBound(from = Some("10.0.0.5"))
    )
  )
```

You can create an `IpRange` aggregation using the `IpRangeAggregation` method this way:
```scala
val aggregation: IpRangeAggregation =
  ipRangeAggregation(
    name = "ip_range_agg",
    field = "ipField",
    ranges = NonEmptyChunk(
      IpRange.IpRangeBound(to = Some("10.0.0.5")),
      IpRange.IpRangeBound(from = Some("10.0.0.5"))
    )
  )
```

You can also use CIDR masks for ranges:
```scala
val cidrAggregation: IpRangeAggregation =
  ipRangeAggregation(
    name = "cidr_agg",
    field = "ipField",
    ranges = NonEmptyChunk(
      IpRange.IpRangeBound(mask = Some("10.0.0.0/25")),
      IpRange.IpRangeBound(mask = Some("10.0.0.128/25"))
    )
  )
```

If you want to explicitly set the keyed property:
```scala
val multipleAggregations =
  ipRangeAggregation("ip_range_agg", "ipField", NonEmptyChunk(IpRange.IpRangeBound(to = Some("10.0.0.5"))))
    .keyedOn
    .withAgg(maxAggregation("maxAgg", "someField"))
```

You can find more information about `Ip Range` aggregation [here](https://www.elastic.co/docs/reference/aggregations/search-aggregations-bucket-iprange-aggregation).