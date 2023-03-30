---
id: overview_bulkable
title: "Bulkable"
---

If you want to use Elasticsearch's Bulk API you can do so using the `bulk` method.
The `bulk` method accepts a sequence of bulkable requests which are `ElasticRequest` that inherit the `Bulkable` trait.
Bulk API for Elasticsearch supports only index, create, delete, and update actions and for that reason,
you can use only `Create`, `CreateOrUpdate`, `CreateWithId` and `DeleteById` in your bulkable requests. 


```scala
final case class User(id: Int, name: String)

object User {
  implicit val schema: Schema.CaseClass2[Int, String, User] =
    DeriveSchema.gen[User]

  val (id, name) = schema.makeAccessors(FieldAccessorBuilder)
}

val requests = List(
  ElasticRequest.create[User](indexName, User(1, "John Doe")),
  ElasticRequest.create[User](indexName, DocumentId("documentId2"), User(2, "Jane Doe")),
  ElasticRequest.upsert[User](indexName, DocumentId("documentId3"), User(3, "Richard Roe")),
  ElasticRequest.deleteById(indexName, DocumentId("documentId2"))
)

for {
  _ <- Elasticsearch.execute(bulk(requests: _*)) 
} yield ()
```
