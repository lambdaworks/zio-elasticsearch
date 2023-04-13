---
id: overview_zio_prelude_schema
title: "Use of ZIO Prelude and Schema"
---

[ZIO Prelude](https://zio.github.io/zio-prelude/docs/overview/overview_index) is a library focused on providing a core set of functional data types and abstractions that can help you solve a variety of day-to-day problems.

### Type-safety with ZIO Prelude's Newtype

Newtypes provide zero overhead new types and refined new types to allow you to increase the type-safety of your code base with zero overhead and minimal boilerplate.
The library uses ZIO Prelude's Newtype for `IndexName`, `DocumentId`, and `Routing` in order to preserve type-safety when these types are being created.

```scala
val indexName: IndexName   = IndexName("index")
val documentId: DocumentId = DocumentId("documentId")
```

### Usage of ZIO Schema and its accessors for type-safety

[ZIO Schema](https://zio.dev/zio-schema/) is a ZIO-based library for modeling the schema of data structures as first-class values.
To provide type-safety in your requests ZIO Elasticsearch uses ZIO Schema.

Query DSL methods that require a field solely accept field types that are defined as Elasticsearch primitives.
You can pass field names simply as strings, or you can use the type-safe query methods that make use of ZIO Schema's accessors.

Here is an example of creating a schema for the custom type `User` and using implicit schema to create accessors which results in type-safe query methods.
You can also represent a field from nested structures with type-safe query methods, using the `/` operator on accessors, as shown below.

If your field name has different naming in Elasticsearch's index then you can use the `@fieldName("...")` annotation, in which case the library
will use the name from the annotation when making the request.

```scala
final case class Address(street: String, number: Int)

object Address {
  implicit val schema: Schema.CaseClass2[String, Int, Address] =
    DeriveSchema.gen[Address]

  val (street, number) = schema.makeAccessors(FieldAccessorBuilder)
}

final case class User(
  @fieldName("_id")
  id: Int,
  email: String,
  address: Address
)

object User {
  implicit val schema: Schema.CaseClass3[Int, String, Address, User] =
    DeriveSchema.gen[User]

  val (id, email, address) = schema.makeAccessors(FieldAccessorBuilder)
}

val query: BoolQuery[User] =
  ElasticQuery
    .must(ElasticQuery.range(User.id).gte(7).lt(10))
    .should(ElasticQuery.startsWith(User.address / Address.street, "ZIO"))

val aggregation: TermsAggregation =
  ElasticAggregation
    .termsAggregation("termsAgg", User.address / Address.street)

val request: SearchAndAggregateRequest =
  ElasticRequest
    .search(IndexName("index"), query)
    .aggregate(aggregation)

val result: RIO[Elasticsearch, SearchResult] = Elasticsearch.execute(request)
```

Accessors also have a `withSuffix` method, in case you want to use one in queries:

```scala
ElasticQuery.term("email.keyword", "jane.doe@lambdaworks.io")

// type-safe method
ElasticQuery.term(User.email.withSuffix("keyword"), "jane.doe@lambdaworks.io")
```

In case the suffix is `"keyword"` or `"raw"` you can use `keyword` and `raw` methods respectively.
