---
id: elastic_query_geo_bounding_box
title: "Geo-bounding-box Query"
---

The `GeoBoundingBox` query matches documents containing geo points that fall within a defined rectangular bounding box, specified by the `top-left` and `bottom-right` corners.

To use the `GeoBoundingBox` query, import the following:
```scala
import zio.elasticsearch.query.GeoBoundingBoxQuery
import zio.elasticsearch.ElasticQuery._
```

You can create a [type-safe](https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema) `GeoBoundingBox` query using typed document fields like this:
```scala
val query: GeoBoundingBoxQuery[Document] =
  geoBoundingBoxQuery(
    field = Document.location,
    topLeft = GeoPoint(40.73, -74.1),
    bottomRight = GeoPoint(40.01, -71.12)
  )
```

You can create a `GeoBoundingBox` query using the `geoBoundingBoxQuery` method with GeoPoints for the `top-left` and `bottom-right` corners:
```scala
val query: GeoBoundingBoxQuery[Any] =
  geoBoundingBoxQuery(
    field = "location",
    topLeft = GeoPoint(40.73, -74.1),
    bottomRight = GeoPoint(40.01, -71.12)
  )
```

If you want to `boost` the relevance score of the query, you can use the `boost` method:
```scala
val queryWithBoost: GeoBoundingBoxQuery[Any] =
  geoBoundingBoxQuery(field = "location", topLeft = GeoPoint(40.73, -74.1), bottomRight = GeoPoint(40.01, -71.12))
    .boost(value = 1.5)
```

To ignore unmapped fields (fields that do not exist in the mapping), use the `ignoreUnmapped` method:
```scala
val queryWithIgnoreUnmapped: GeoBoundingBoxQuery[Any] =
  geoBoundingBoxQuery(field = "location", topLeft = GeoPoint(40.73, -74.1), bottomRight = GeoPoint(40.01, -71.12))
    .ignoreUnmappedTrue
```

If you want to specify a query name, you can use the `name` method:
```scala
val queryWithName: GeoBoundingBoxQuery[Any] =
  geoBoundingBoxQuery(field = "location", topLeft = GeoPoint(40.73, -74.1), bottomRight = GeoPoint(40.01, -71.12))
    .name(value = "name")
```

If you want to specify the `validation_method`, you can use the `validationMethod` method:
```scala
import zio.elasticsearch.query.ValidationMethod

val queryWithValidationMethod: GeoBoundingBoxQuery[Any] =
  geoBoundingBoxQuery(field = "location", topLeft = GeoPoint(40.73, -74.1), bottomRight = GeoPoint(40.01, -71.12))
    .validationMethod(value = ValidationMethod.IgnoreMalformed)
```

You can find more information about `GeoBoundingBox` query [here](https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-geo-bounding-box-query).
