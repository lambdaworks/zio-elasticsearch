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
