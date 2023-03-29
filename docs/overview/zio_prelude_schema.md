---
id: overview_zio_prelude_schema
title: "Use of ZIO Prelude and Schema"
---

### Typesafety with ZIO-prelude's NewType

We use ZIO-prelude's NewType for `IndexName`, `DocumentId` and `Routing` in order to preserve type safety and have strings validated when these types are being created.

```scala
val indexName: IndexName = IndexName("test-es-index")
val docId: DocumentId = DocumentId("document-id")
```

### Usage of ZIO Schema and its accessors for type safety

To provide type safety in your requests zio-elasticsearch uses ZIO Schema. Here is an example of creating schema for custom type `User` and using implicit schema to create accessors which results in type safe request and response.

```scala
final case class Address(street: String, number: Int)

object Address {

  implicit val schema: Schema.CaseClass2[String, Int, Address] =
    DeriveSchema.gen[Address]

  val (street, number) = schema.makeAccessors(FieldAccessorBuilder)
}

case class User(id: Int, address: Address)

object User {
  implicit val schema: Schema.CaseClass2[String, Address, User] =
    DeriveSchema.gen[User]

  val (id, address) = schema.makeAccessors(FieldAccessorBuilder)
}

for {
  _ <- Elasticsearch.execute(
    search(
      IndexName("index-name"),
      must(range(User.id).gte(7).lt(10)).should(startsWith(User.address.name, "ZIO"))
    ).aggregate(aggregation)
  )
} yield ()
```
