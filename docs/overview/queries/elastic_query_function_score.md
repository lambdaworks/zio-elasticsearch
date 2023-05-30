---
id: elastic_query_function_score
title: "Function Score Query"
---

The `FunctionScore` allows you to modify the score of documents that are retrieved by a query.

In order to use the `FunctionScore` query and create needed `FunctionScoreFunction` import the following:
```scala
import zio.elasticsearch.query.FunctionScoreQuery
import zio.elasticsearch.query.FunctionScoreFunction._
import zio.elasticsearch.ElasticQuery._
```

For creating `FunctionScore` query you require `FunctionScoreFunction` or multiple of them. 
You can create these functions in following way.

<br/>

You can create `DecayFunction` with `DecayFunctionType.Exp` using the `expDecayFunction` method with origin and scale in the following manner:
```scala
val function: DecayFunction[Any] = expDecayFunction("field", origin = "11, 12", scale = "2km")
```
You can create a [type-safe](https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema)
`DecayFunction` with `DecayFunctionType.Exp` using the `expDecayFunction` method with origin and scale in the following manner:
```scala
val function: DecayFunction[Document] = expDecayFunction(field = Document.field, origin = "11, 12", scale = "2km")
```

<br/>

You can create `DecayFunction` with `DecayFunctionType.Gauss` using the `gaussDecayFunction` method with origin and scale in the following manner:
```scala
val function: DecayFunction[Any] = gaussDecayFunction(field = "field", origin = "11, 12", scale = "2km")
```
You can create a [type-safe](https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema)
`DecayFunction` with `DecayFunctionType.Gauss` using the `gaussDecayFunction` method with origin and scale in the following manner:
```scala
val function: DecayFunction[Document] = gaussDecayFunction(field = Document.field, origin = "11, 12", scale = "2km")
```

<br/>

You can create `DecayFunction` with `DecayFunctionType.Linear` using the `linearDecayFunction` method with origin and scale in the following manner:
```scala
val function: DecayFunction[Any] = linearDecayFunction(field = "field", origin = "11, 12", scale = "2km")
``` 

You can create a [type-safe](https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema)
`DecayFunction` with `DecayFunctionType.Linear` using the `expDecayFunction` method with origin and scale in the following manner:
```scala
val function: DecayFunction[Document] = linearDecayFunction(field = Document.field, origin = "11, 12", scale = "2km")
```

<br/>

You can create `FieldValueFactor` using the `fieldValueFactor` method:

```scala
val function: FieldValueFactor[Any] = fieldValueFactor(field = "field")
``` 

You can create a [type-safe](https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema)
`FieldValueFactor` using the `fieldValueFactor` method:

```scala
val function: FieldValueFactor[Document] = fieldValueFactor(field = Document.field)
``` 

<br/>

You can create `RandomScoreFunction` using the `randomScoreFunction` in three different ways depending on amount of parameters
you want to use:

```scala
val function: RandomScoreFunction[Any] = randomScoreFunction()

val function: RandomScoreFunction[Any] = randomScoreFunction(seed = 10)

val function: RandomScoreFunction[Any] = randomScoreFunction(seed = 10, field = "field")
``` 

You can create a [type-safe](https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema)
`RandomScoreFunction` using the `randomScoreFunction` method:
```scala
val function: RandomScoreFunction = randomScoreFunction(seed = 10, field = Document.field)
```

<br/>

You can create `ScriptScoreFunction` using the `scriptScoreFunction` method with script in following manner:

```scala
val function: ScriptScoreFunction[Any] = scriptScoreFunction(script = Script("params.agg1 > 10"))
val function: ScriptScoreFunction[Any] = scriptScoreFunction(scriptSource = "params.agg1 > 10")
``` 

<br/>

You can create `WeightFunction` using the `weightFunction` method(you must provide `Any` type parameter when using):

```scala
val function: WeightFunction[Any] = scriptScoreFunction(weight = 10)
``` 

<br/><br/>

You can use these functions to create `FunctionScore` query using the `functionScore` method in the following manner:

```scala
val randomScoreFunction: RandomScoreFunction[Any] = randomScoreFunction()
val weightFunction: WeightFunction[Any] = scriptScoreFunction(weight = 10)
val query: FunctionScoreQuery[Any] = functionScore(randomScoreFunction, weightFunction)
```

You can create a [type-safe](https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema) `FunctionScore` query 
using the `functionScore`, if all functions are created type-safe, in the following manner:

```scala
val decayFunction: DecayFunction[Document] = expDecayFunction(field = Document.field, origin = "11, 12", scale = "2km")
val randomScoreFunction: RandomScoreFunction[Document] = randomScoreFunction(seed = 10, field = Document.field)
val weightFunction: WeightFunction[Any] = scriptScoreFunction(weight = 10)
val query: FunctionScoreQuery[Document] = functionScore(decayFunction, randomScoreFunction, weightFunction)
```

<br/>

If you want to change the `boost`, you can use `boost` method:
```scala
import zio.elasticsearch.query.DistanceUnit

val queryWithDistance: FunctionScoreQuery[Document] = functionScore(randomScoreFunction(seed = 10, field = Document.field)).boost(5.0)
```

<br/>

If you want to change the `boostMode`, you can use `boostMode` method:
```scala
import zio.elasticsearch.query.DistanceUnit

val queryWithDistance: FunctionScoreQuery[Document] = functionScore(randomScoreFunction(seed = 10, field = Document.field)).boostMode(FunctionScoreBoostMode.Max)
```

<br/>

If you want to change the `maxBoost`, you can use `maxBoost` method:
```scala
import zio.elasticsearch.query.DistanceUnit

val queryWithDistance: FunctionScoreQuery[Document] = functionScore(randomScoreFunction(seed = 10, field = Document.field)).maxBoost(5.0)
```

<br/>

If you want to change the `minScore`, you can use `minScore` method:
```scala
import zio.elasticsearch.query.DistanceUnit

val queryWithDistance: FunctionScoreQuery[Document] = functionScore(randomScoreFunction(seed = 10, field = Document.field)).minScore(5.0)
```

<br/>

If you want to change the `query`, you can use `query` method:
```scala
import zio.elasticsearch.query.DistanceUnit

val queryWithDistance: FunctionScoreQuery[Document] = functionScore(randomScoreFunction(seed = 10, field = Document.field)).query(matches(Document.field, "value"))
```

<br/>

If you want to change the `scoreMode`, you can use `scoreMode` method:
```scala
import zio.elasticsearch.query.DistanceUnit

val queryWithDistance: FunctionScoreQuery[Document] = functionScore(randomScoreFunction(seed = 10, field = Document.field)).scoreMode(FunctionScoreScoreMode.Max)
```

<br/>

If you want to add a one or multiple new `FunctionScoreFunction` you can use `withFunctions` method:
```scala
import zio.elasticsearch.query.DistanceUnit

val queryWithDistance: FunctionScoreQuery[Document] = functionScore(randomScoreFunction(seed = 10, field = Document.field)).withFunctions(scriptScoreFunction(weight = 10))
```

You can find more information about `FunctionScore` query [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/query-dsl-function-score-query.html).
