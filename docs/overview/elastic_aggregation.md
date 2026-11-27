---
id: elastic_aggregation
title: "Overview"
---

In order to execute Elasticsearch aggregation requests, you first must specify the type of the aggregation along with the corresponding parameters for that type.
Aggregations are described with the `ElasticAggregation` data type, which can be constructed from the DSL methods found under the following import:

```scala
import zio.elasticsearch.ElasticAggregation._
```

Aggregation DSL methods that require a field solely accept field types that are defined as Elasticsearch primitives.
You can pass field names simply as strings, or you can use the type-safe aggregation methods that make use of ZIO Schema's accessors.
An example with a `max` aggregation is shown below:

```scala
import zio.elasticsearch.ElasticAggregation._

final case class User(id: Int, name: String, age: Int)

object User {
  implicit val schema: Schema.CaseClass3[Int, String, Int, User] =
    DeriveSchema.gen[User]

  val (id, name, age) = schema.makeAccessors(FieldAccessorBuilder)
}

maxAggregation(name = "maxAggregation", field = "age")

// type-safe method
maxAggregation(name = "maxAggregation", field = User.age)
```

You can also represent a field from nested structures with type-safe aggregation methods, using the `/` operator on accessors:

```scala
import zio._
import zio.elasticsearch._
import zio.elasticsearch.ElasticAggregation._
import zio.schema.annotation.fieldName
import zio.schema.{DeriveSchema, Schema}

final case class Name(
  @fieldName("first_name")
  firstName: String,
  @fieldName("last_name")
  lastName: String
)

object Name {
  implicit val schema: Schema.CaseClass2[String, String, Name] = DeriveSchema.gen[Name]

  val (firstName, lastName) = schema.makeAccessors(FieldAccessorBuilder)
}

final case class User(id: String, name: Name, email: String, age: Int)

object User {
  implicit val schema: Schema.CaseClass4[String, Name, String, Int, User] = 
    DeriveSchema.gen[User]

  val (id, name, email, age) = schema.makeAccessors(FieldAccessorBuilder)
}

termsAggregation(name = "termsAggregation", field = "name.first_name")

// type-safe method
termsAggregation(name = "termsAggregation", field = User.name / Name.firstName)
```

Accessors also have a `suffix` method, in case you want to use one in aggregations:

```scala
ElasticAggregation.cardinality(name = "cardinalityAggregation", field = "email.keyword")

// type-safe method
ElasticAggregation.cardinality(name = "cardinalityAggregation", field = User.email.suffix("keyword"))
```

In case the suffix is `"keyword"` or `"raw"` you can use `keyword` and `raw` methods respectively.

Aggregations are divided into three groups:

- **metric** aggregations (e.g. `avg`, `max`, `stats`) compute metrics from field values,
- **bucket** aggregations group documents into buckets and accept sub-aggregations through `withSubAgg`. They are either multi-bucket (`terms`, `range`, `ipRange`) or single-bucket (`filter`, `missing`, `sampler`),
- **pipeline** aggregations (e.g. `bucketSelector`, `bucketSort`) work on the output of other aggregations.

Metric and bucket aggregations can be combined on the same level with `withAgg`, which returns `MultipleAggregations`.
Aggregations on the same level, and sub-aggregations of the same aggregation, must have unique names.
Parent pipeline aggregations, such as `bucketSelector` and `bucketSort`, work on the buckets of their parent aggregation, so they can only be added as sub-aggregations of a multi-bucket aggregation.
Using them on the top level of a request, or as a sub-aggregation of a single-bucket aggregation, is rejected at compile time:

```scala
import zio.elasticsearch.ElasticAggregation._

// compiles
termsAggregation(name = "termsAggregation", field = "name.first_name.keyword")
  .withSubAgg(maxAggregation(name = "maxAge", field = "age"))
  .withSubAgg(bucketSortAggregation(name = "bucketSort").size(5))

// does not compile
maxAggregation(name = "maxAge", field = "age").withAgg(bucketSortAggregation(name = "bucketSort"))
missingAggregation(name = "missingAggregation", field = "age").withSubAgg(bucketSortAggregation(name = "bucketSort"))
```

Every aggregation also supports the `meta` method, which adds custom metadata that Elasticsearch returns together with the aggregation result:

```scala
maxAggregation(name = "maxAggregation", field = "age").meta("color", "blue")
```

Now, after describing an aggregation, you can pass it to the `aggregate`/`search` method to obtain the `ElasticRequest` corresponding to that aggregation:

```scala
import zio.elasticsearch.ElasticAggregation._
import zio.elasticsearch.ElasticQuery._

ElasticRequest.aggregate(selectors = IndexName("index"), aggregation = termsAggregation(name = "termsAggregation", field = "name.first_name.keyword"))
ElasticRequest.search(selectors = IndexName("index"), query = matchAll, aggregation = termsAggregation(name = "termsAggregation", field = "name.first_name.keyword"))

// type-safe methods
ElasticRequest.aggregate(selectors = IndexName("index"), aggregation = termsAggregation(name = "termsAggregation", field = User.name / Name.firstName.keyword))
ElasticRequest.search(selectors = IndexName("index"), query = matchAll, aggregation = termsAggregation(name = "termsAggregation", field = User.name / Name.firstName.keyword))

```

## Aggregation results

Executing an `aggregate` or `search` request with an aggregation gives access to the aggregation results by name, either on the effect or on the result itself. Each supported aggregation has a typed accessor (`asAvgAggregation`, `asFilterAggregation`, `asTermsAggregation`, ...), and `aggregationAs` works for any result type:

```scala
import zio.elasticsearch.result.{MaxAggregationResult, TermsAggregationResult}

val aggregation =
  termsAggregation(name = "termsAggregation", field = "name.first_name.keyword")
    .withSubAgg(maxAggregation(name = "maxAge", field = "age"))
    .meta("color", "blue")

val terms: RIO[Elasticsearch, Option[TermsAggregationResult]] =
  Elasticsearch
    .execute(ElasticRequest.aggregate(selectors = IndexName("index"), aggregation = aggregation))
    .asTermsAggregation("termsAggregation")
```

An accessor returns `None` if there is no aggregation with the given name, and fails with a `DecodingException` if the aggregation is of another type.

Results of multi-bucket aggregations (`terms`, `range`, `ip_range`) contain `buckets`, each with its `key` and `docCount`. Results of single-bucket aggregations (`filter`, `missing`, `sampler`) contain their `docCount`. Buckets and single-bucket results give access to the results of their sub-aggregations with `subAggregationAs`:

```scala
val maxAges: RIO[Elasticsearch, Chunk[Option[MaxAggregationResult]]] =
  terms.flatMap { result =>
    ZIO.foreach(Chunk.fromIterable(result).flatMap(_.buckets)) { bucket =>
      ZIO.fromEither(bucket.subAggregationAs[MaxAggregationResult]("maxAge"))
    }
  }
```

Every result also contains the `meta` that was set on its aggregation (an empty map if none was set).
