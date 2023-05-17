---
id: elastic_query_geo_distance
title: "Geo-distance Query"
---

The Geo-distance query matches [geo_point](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/geo-point.html) and [geo_shape](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/geo-shape.html) values within a given distance of a geopoint.

In order to use the `GeoDistance` query import following:

```scala
import zio.elasticsearch.query.GeoDistanceQuery
import zio.elasticsearch.ElasticQuery._
```

To create a type-safe instance of `GeoDistanceQuery` with latitude and longitude:
```scala
val query: GeoDistanceQuery = geoDistance(field = Document.location, latitude = 20.0, longitude = 20.0)
```

To create an instance of `GeoDistanceQuery` with latitude and longitude:
```scala
val query: GeoDistanceQuery = geoDistance(field = "location", latitude = 20.0, longitude = 20.0)
```

To create a type-safe of `GeoDistanceQuery` instance with coordinates:
```scala
val query: GeoDistanceQuery = geoDistance(field = Document.location, coordinates = "40,31")
```

To create an instance of `GeoDistanceQuery` with coordinates:
```scala
val query: GeoDistanceQuery = geoDistance(field = "location", coordinates = "40,31")
```

If you want to change the `distance`, you can use `distance` method:
```scala
import zio.elasticsearch.query.DistanceUnit

val queryWithDistance: GeoDistanceQuery = geoDistance(field = "location", coordinates = "40,31").distance(value = 20.0, unit = DistanceUnit.Kilometers)
```

If you want to change the `distance_type`, you can use `distanceType` method:
```scala
import zio.elasticsearch.query.DistanceType

val queryWithDistanceType: GeoDistanceQuery = geoDistance(field = "location", coordinates = "40,31").distanceType(value = DistanceType.Plane)
```

If you want to change the `_name`, you can use `name` method:
```scala
val queryWithName: GeoDistanceQuery = geoDistance(field = "location", coordinates = "40,31").name("name")
```

If you want to change the `validation_method`, you can use `validationMethod` method:
```scala
import zio.elasticsearch.query.ValidationMethod

val queryWithValidationMethod: GeoDistanceQuery = geoDistance(field = "location", coordinates = "40,31").validationMethod(value = ValidationMethod.IgnoreMalformed)
```

You can find more information about Geo-distance query [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/query-dsl-geo-distance-query.html#query-dsl-geo-distance-query).
