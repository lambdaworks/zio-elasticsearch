---
id: overview_zio_prelude_schema
title: "Use of ZIO Prelude and Schema"
---

[ZIO Prelude](https://zio.github.io/zio-prelude/docs/overview/overview_index) is a library focused on providing a core set of functional data types and abstractions that can help you solve a variety of day to day problems.

### Type-safety with ZIO-prelude's NewType

New Types provide zero overhead newtypes and refined newtypes to allow you to increase the type safety of your code base with zero overhead and minimal boilerplate.
The library uses ZIO Prelude's NewType for `IndexName`, `DocumentId` and `Routing` in order to preserve type safety and have strings validated when these types are being created.

```scala
val indexName: IndexName = IndexName("test-es-index")
val docId: DocumentId = DocumentId("document-id")
```

### Usage of ZIO Schema and its accessors for type safety

[ZIO Schema](https://zio.dev/zio-schema/) is a ZIO-based library for modeling the schema of data structures as first-class values.
To provide type safety in your requests zio-elasticsearch uses ZIO Schema.

Query DSL methods that require a field solely accept field types that are defined as Elasticsearch primitives.
You can pass field names simply as strings, or you can use the type-safe query methods that make use of ZIO Schema's accessors.

Here is an example of creating schema for custom type `User` and using implicit schema to create accessors which results in type safe query methods.
You can also represent a field from nested structures with type-safe query methods, using the `/` operator on accessors, as shown below.

If your field name has different naming in Elasticsearch index then you can use `@fieldName("")` annotation, in which case library
will use name from annotation when making request as you can see in `User` case class for `id` field

```scala
final case class Address(street: String, number: Int)

object Address {

  implicit val schema: Schema.CaseClass2[String, Int, Address] =
    DeriveSchema.gen[Address]

  val (street, number) = schema.makeAccessors(FieldAccessorBuilder)
}

case class User(
  @fieldName("_id")
  id: Int,
  address: Address
)

object User {
  implicit val schema: Schema.CaseClass2[Int, Address, User] =
    DeriveSchema.gen[User]

  val (id, address) = schema.makeAccessors(FieldAccessorBuilder)
}

for {
  _ <- Elasticsearch.execute(
    search(
      IndexName("index-name"),
      must(range(User.id).gte(7).lt(10)).should(startsWith(User.address / Address.street, "ZIO"))
    ).aggregate(aggregation)
  )
} yield ()
```

Type-safe query methods also have a `multiField` parameter, in case you want to use one in queries:

```scala
term("degree.keyword", "baz")

// type-safe method
term(EmployeeDocument.degree, multiField = Some("keyword"), "baz")
```
