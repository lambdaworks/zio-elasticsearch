---
id: overview_elastic_query
title: "Elastic Query"
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
term("name", "foo bar")

// type-safe method
term(User.name, "foo bar")
```

You can also represent a field from nested structures with type-safe query methods, using the `/` operator on accessors:

```scala
import zio.elasticsearch.ElasticQueryAccessorBuilder
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

matches("name.first_name", "John")

// type-safe method
matches(User.name / Name.firstName, "John")
```

Type-safe query methods also have a `multiField` parameter, in case you want to use one in queries:

```scala
term("email.keyword", "jane.doe@lambdaworks.io")

// type-safe method
term(User.email, multiField = Some("keyword"), "jane.doe@lambdaworks.io")
```

Now, after describing a query, you can pass it to the `search`/`deleteByQuery` method to obtain the `ElasticRequest` corresponding to that query:

```scala
search(IndexName("index"), term("name.first_name.keyword", "John"))

// type-safe method
search(IndexName("index"), term(User.name / Name.firstName, multiField = Some("keyword"), "John"))
```
