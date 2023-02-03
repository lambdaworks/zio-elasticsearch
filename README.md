![scala-version][scala-version-badge]
[![CI](https://github.com/lambdaworks/zio-elasticsearch/actions/workflows/ci.yml/badge.svg)](https://github.com/lambdaworks/zio-elasticsearch/actions/workflows/ci.yml)
[![Sonatype Snapshots](https://img.shields.io/nexus/s/https/s01.oss.sonatype.org/io.lambdaworks/zio-elasticsearch_2.13.svg?label=Sonatype%20Snapshot)](https://s01.oss.sonatype.org/content/repositories/snapshots/io/lambdaworks/zio-elasticsearch_2.13/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

# ZIO Elasticsearch

## Overview

ZIO Elasticsearch is a type-safe, testable and streaming-friendly ZIO native Elasticsearch client.

The library depends on sttp as an HTTP client for executing requests, and other ZIO libraries such as ZIO Schema and ZIO Prelude.

The following versions are supported:
- Scala: 2.12+
- ZIO: 2
- Elasticsearch: 7
- JVM 11+

## Installation

To use ZIO Elasticsearch in your project, add the following to your `build.sbt` file:

```scala
resolvers += "Sonatype OSS Snapshots" at "https://s01.oss.sonatype.org/content/repositories/snapshots"

libraryDependencies += "io.lambdaworks" %% "zio-elasticsearch" % "<snapshot version>"
```

Where `<snapshot version>` refers to the version in the Sonatype Snapshot badge above.

## Usage

### Elastic Request

We can represent an Elasticsearch request as a generic data type that returns a value of type `A`. The library offers a DSL for creating these requests, by specifying their required parameters. For example, we can create a request for deleting a document with a specified index as follows:

```scala
deleteById(IndexName("index"), DocumentId("documentId"))
```

As you can see above, index names and document IDs are represented with `IndexName` and `DocumentId` respectively, using new types from ZIO Prelude, in order to increase type-safety with no runtime overhead. `IndexName` also validates the passed string according to Elasticsearch's naming criteria at compile-time using the `apply` method, or with `make` at runtime when dealing with a runtime value as an argument.

All the DSL methods for request creation can be brought into scope with the following import:

```scala
import zio.elasticsearch.ElasticRequest._
```

For methods receiving or returning a document of custom type `A`, you must create a schema for `A`. Here is an example of creating a schema for a custom type `EmployeeDocument`:

```scala
import zio.schema.{DeriveSchema, Schema}

final case class EmployeeDocument(id: String, name: String, degree: String, age: Int)

object EmployeeDocument {
  implicit val schema: Schema[EmployeeDocument] = DeriveSchema.gen[EmployeeDocument]
}
```

As long as we have the implicit schema value in scope, we can call the aforementioned methods, such as `getById`:

```scala
import EmployeeDocument._

getById[EmployeeDocument](IndexName("index"), DocumentId("documentId"))
```

### Elastic Query

In order to execute Elasticsearch query requests, both for searching and deleting by query, you first must specify the type of the query along with the corresponding parameters for that type. Queries are described with the `ElasticQuery` data type, which can be constructed from the DSL methods found under the following import:

```scala
import zio.elasticsearch.ElasticQuery._
```

Query DSL methods that require a field solely accept field types that are defined as Elasticsearch primitives. You can pass field names simply as strings, or you can use the type-safe query methods that make use of ZIO Schema's accessors. An example with a `term` query is shown below:

```scala
term("name", "foo bar")

// type-safe method
term(EmployeeDocument.name, "foo bar")
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
  implicit val schema = DeriveSchema.gen[Name]

  val (firstName, lastName) = schema.makeAccessors(ElasticQueryAccessorBuilder)
}

final case class EmployeeDocument(id: String, name: Name, degree: String, age: Int)

object EmployeeDocument {
  implicit val schema = DeriveSchema.gen[EmployeeDocument]

  val (id, name, degree, age) = schema.makeAccessors(ElasticQueryAccessorBuilder)
}

matches("name.first_name", "foo")

// type-safe method
matches(EmployeeDocument.name / Name.firstName, "foo bar")
```

Type-safe query methods also have a `multiField` parameter, in case you want to use one in queries:

```scala
term("degree.keyword", "baz")

// type-safe method
term(EmployeeDocument.degree, multiField = Some("keyword"), "baz")
```

Now, after describing a query, you can pass it to the `search`/`deleteByQuery` method to obtain the Elastic request corresponding to that query:

```scala
search(IndexName("index"), term("name.first_name.keyword", "foo"))
```

### Fluent API

Both Elastic requests and queries offer a fluent API, so that you can provide optional parameters in chained method calls for each request or query. For example, if we wanted to add routing and refresh parameters to a `deleteById` request:

```scala
deleteById(IndexName("index"), DocumentId("documentId")).routing(Routing("routing")).refreshTrue
```

Just like `IndexName`, `Routing` is a new type that mustn't be an empty string.

And if we wanted to specify lower and upper bounds for a `range` query:

```scala
range(EmployeeDocument.age).gte(18).lt(100)
```

### Elastic Executor

In order to get the functional effect of executing a specified Elasticsearch request, you must call the `execute` method defined on it, which returns a `ZIO` that requires an `ElasticExecutor`, fails with a `Throwable` and returns the relevant value `A` for that request.

Elastic requests for creating and deleting return `CreationOutcome` and `DeletionOutcome` respectively if no other meaningful value could be returned, notifying us on the success of the request. Any other kind of error is returned as a `Throwable` in the error channel of `ZIO` for that Elastic request.

If you want to execute multiple Elasticsearch requests in a single API call, you need to use the `bulk` method on those Elastic requests, and call `execute` on that bulk request instead.

To provide the dependency on `ElasticExecutor`, you must pass one of the `ZLayer`s from the following import:

```scala
import zio.elasticsearch.ElasticExecutor
```

For example, if you want to execute requests on a server running on `localhost` and port `9200`, you can provide the `live` ZLayer to your effect, along with a `SttpBackend` and an `ElasticConfig` layer:

```scala
import sttp.client3.SttpBackend
import sttp.client3.httpclient.zio.HttpClientZioBackend
import zio.elasticsearch.{ElasticConfig, ElasticExecutor}

val effect: RIO[ElasticExecutor, Boolean] = exists(IndexName("index"), DocumentId("document")).execute

effect.provide(
  HttpClientZioBackend.layer(),
  ZLayer.succeed(ElastichConfig("localhost", 9200)) >>> ElasticExecutor.live,
)
```

If the ElasticConfig arguments are the same as specified above, you can simply omit the `ElasticConfig` layer and replace `ElasticExecutor.live` with `ElasticExecutor.local` instead.

For testing purposes, you can use `ElasticExecutor.test`, which is a mocked Elasticsearch executor that doesn't require an HTTP backend.

```scala
// The Elasticsearch requests are executed locally
effect.provide(
  HttpClientZioBackend.layer(),
  ElasticExecutor.local
)

// The Elasticsearch requests are executed on a mocked executor
effect.provideLayer(ElasticExecutor.test)
```

## Example

For a full-fledged example using this library, you can check out the [example](modules/example) module, which contains an application with both a description and instructions on how to run it.

## License
[License](LICENSE)


[scala-version-badge]: https://img.shields.io/badge/scala-2.13.10-blue?logo=scala&color=red
