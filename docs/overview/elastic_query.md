---
id: elastic_query
title: "Overview"
---

In order to execute Elasticsearch query requests, both for searching and deleting by query, 
you first must specify the type of the query along with the corresponding parameters for that type. 
Queries are described with the `ElasticQuery` data type, which can be constructed from the DSL methods found under the following import:

```scala
import zio.elasticsearch.ElasticQuery._
```

Query DSL methods that require a field solely accept field types that are defined as Elasticsearch primitives.
You can pass field names simply as strings, or you can use the type-safe query methods that make use of ZIO Schema's accessors. 
An example with a `term` query is shown below:

```scala
final case class User(id: Int, name: String)

object User {
  implicit val schema: Schema.CaseClass2[Int, String, User] =
    DeriveSchema.gen[User]

  val (id, name) = schema.makeAccessors(FieldAccessorBuilder)
}

term("name", "John Doe")

// type-safe method
term(field = User.name, value = "John Doe")
```

You can also represent a field from nested structures with type-safe query methods, using the `/` operator on accessors:

```scala
import zio._
import zio.elasticsearch._
import zio.elasticsearch.ElasticQuery._
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

matches(field = "name.first_name", value = "John")

// type-safe method
matches(field = User.name / Name.firstName, value = "John")
```

Accessors also have a `withSuffix` method, in case you want to use one in queries:

```scala
ElasticQuery.term("email.keyword", "jane.doe@lambdaworks.io")

// type-safe method
ElasticQuery.term(User.email.withSuffix("keyword"), "jane.doe@lambdaworks.io")
```

In case the suffix is `"keyword"` or `"raw"` you can use `keyword` and `raw` methods respectively.

Now, after describing a query, you can pass it to the `search`/`deleteByQuery` method to obtain the `ElasticRequest` corresponding to that query:

```scala
ElasticRequest.search(IndexName("index"), term(field = "name.first_name.keyword", value = "John"))

// type-safe method
ElasticRequest.search(IndexName("index"), term(field = User.name / Name.firstName.keyword, value = "John"))
```
