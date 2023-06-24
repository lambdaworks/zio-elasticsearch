---
id: elastic_query_geo_polygon
title: "Geo-polygon Query"
---

A query returning hits that only fall within a polygon of points.

In order to use the `GeoPolygon` query import the following:
```scala
import zio.elasticsearch.query.GeoPolygonQuery
import zio.elasticsearch.ElasticQuery._
```

You can create a `GeoPolygon` query using the `geoPolygon` method with list of coordinates in the following manner:
```scala
val query: GeoPolygonQuery = geoPolygon(field = "location", List("0, 0", "0, 90", "90, 90", "90, 0"))
```

You can create a [type-safe](https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema) `GeoPolygon` query using the `geoPolygon` method with list of coordinates in the following manner:
```scala
val query: GeoPolygonQuery = geoPolygon(field = Document.location, List("0, 0", "0, 90", "90, 90", "90, 0"))
```


If you want to change the `_name`, you can use `name` method:
```scala
val queryWithName: GeoPolygonQuery = geoPolygon(field = "location", coordinates = List("0, 0", "0, 90", "90, 90", "90, 0")).name("name")
```

If you want to change the `validation_method`, you can use `validationMethod` method:
```scala
import zio.elasticsearch.query.ValidationMethod

val queryWithValidationMethod: GeoPolygonQuery = geoPolygon(field = "location", coordinates =  List("0, 0", "0, 90", "90, 90", "90, 0")).validationMethod(value = ValidationMethod.IgnoreMalformed)
```

You can find more information about `GeoPolygon` query [here](https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-geo-polygon-query.html).
