---
id: elastic_query_geo_distance
title: "Geo-distance Query"
---

The `GeoDistance` query matches [geo_point](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/geo-point.html) and [geo_shape](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/geo-shape.html) values within a given distance of a geopoint.

In order to use the `GeoDistance` query import the following:
```scala
import zio.elasticsearch.query.GeoDistanceQuery
import zio.elasticsearch.ElasticQuery._
```

You can create a `GeoDistance` query using the `geoDistance` method with a GeoPoint in the following manner:
```scala
val query: GeoDistanceQuery =
  geoDistance(field = "location", point = GeoPoint(20.0, 20.0), distance = Distance(200, Kilometers))
```

You can create a [type-safe](https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema) `GeoDistance` query using the `geoDistance` method with latitude and longitude in the following manner:
```scala
val query: GeoDistanceQuery =
  geoDistance(field = Document.location, point = GeoPoint(20.0, 20.0), distance = Distance(200, Kilometers))
```

If you want to specify the `distance_type`, you can use the `distanceType` method:
```scala
import zio.elasticsearch.query.DistanceType

val queryWithDistanceType: GeoDistanceQuery = 
  geoDistance(field = "location", point = GeoPoint(20.0, 20.0), distance = Distance(200, Kilometers))
    .distanceType(value = DistanceType.Plane)
```

If you want to specify a query name, you can use the `name` method:
```scala
val queryWithName: GeoDistanceQuery =
  geoDistance(field = "location", point = GeoPoint(20.0, 20.0), distance = Distance(200, Kilometers)).name("name")
```

If you want to specify the `validation_method`, you can use the `validationMethod` method:
```scala
import zio.elasticsearch.query.ValidationMethod

val queryWithValidationMethod: GeoDistanceQuery =
  geoDistance(field = "location", point = GeoPoint(20.0, 20.0), distance = Distance(200, Kilometers))
    .validationMethod(value = ValidationMethod.IgnoreMalformed)
```

You can also specify the point as a geo-hash:
```scala
import zio.elasticsearch.query.ValidationMethod

val queryWithValidationMethod: GeoDistanceQuery =
  geoDistance(field = "location", point = GeoHash("drm3btev3e86"), distance = Distance(200, Kilometers))
```

You can find more information about `GeoDistance` query [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/query-dsl-geo-distance-query.html#query-dsl-geo-distance-query).
