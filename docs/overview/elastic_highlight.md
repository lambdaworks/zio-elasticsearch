---
id: overview_elastic_highlight
title: "Highlights"
---

Highlights allow you to get highlighted snippets from one or more fields in your search results, so you can show users where the query matches are.
Highlights are constructed from the DSL methods found under the following import:
```scala
import zio.elasticsearch.ElasticHighlight._
```

The type of the constructed highlights is internal to the library, so it can't be written out explicitly; let the compiler infer it, as in the examples below.

You can highlight a field using the `highlight` method this way:
```scala
val highlights = highlight(field = "stringField")
```

You can highlight a [type-safe](https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema) field using the `highlight` method this way:
```scala
val highlights = highlight(field = Document.stringField)
```

If you want to specify options for a highlighted field (e.g. `type`, `fragment_size`, `number_of_fragments`), you can pass a configuration map to the `highlight` method:
```scala
import zio.json.ast.Json.{Num, Str}

val highlightsWithConfig = highlight(field = Document.stringField, config = Map("type" -> Str("plain"), "fragment_size" -> Num(20)))
```

If you want to highlight more fields, you can use `withHighlight` method (with or without field-specific configuration):
```scala
val highlightsWithMultipleFields =
  highlight(field = Document.stringField)
    .withHighlight(field = Document.titleField)
    .withHighlight(field = "descriptionField", config = Map("number_of_fragments" -> Num(0)))
```

If you want to specify options that apply to all highlighted fields, you can use `withGlobalConfig` method:
```scala
import zio.json.ast.Json.{Arr, Str}

val highlightsWithGlobalConfig =
  highlight(field = Document.stringField)
    .withGlobalConfig(field = "pre_tags", value = Arr(Str("<b>")))
    .withGlobalConfig(field = "post_tags", value = Arr(Str("</b>")))
```

Field-specific configuration overrides global configuration:
```scala
val highlightsWithGlobalAndFieldConfig =
  highlight(field = Document.stringField, config = Map("pre_tags" -> Arr(Str("<i>")), "post_tags" -> Arr(Str("</i>"))))
    .withHighlight(field = Document.titleField)
    .withGlobalConfig(field = "pre_tags", value = Arr(Str("<b>")))
    .withGlobalConfig(field = "post_tags", value = Arr(Str("</b>")))
```

By default, highlighted fields are sent to Elasticsearch as an object, so their order is not guaranteed.
If you want highlighted fields to be in the order they were specified, you can use `withExplicitFieldOrder` method:
```scala
val highlightsWithExplicitFieldOrder =
  highlight(field = Document.stringField)
    .withHighlight(field = Document.titleField)
    .withExplicitFieldOrder
```

## Using highlights

Highlights can be added to the [`Search` and `SearchAndAggregate`](https://lambdaworks.github.io/zio-elasticsearch/overview/requests/elastic_request_search) requests using the `highlights` method:
```scala
import zio.elasticsearch._
import zio.elasticsearch.ElasticQuery.matches
import zio.elasticsearch.ElasticRequest.{search, SearchRequest}

val request: SearchRequest =
  search(selectors = IndexName("index"), query = matches(field = Document.stringField, value = "test"))
    .highlights(highlight(field = Document.stringField))
```

Highlights can also be added to the inner hits of the [`Nested`](https://lambdaworks.github.io/zio-elasticsearch/overview/queries/elastic_query_nested) query.
In that case, both the fields of the inner query and the highlighted fields are relative to the nested path (`stringField` below is matched and highlighted as `subDocumentList.stringField`):
```scala
import zio.elasticsearch.ElasticQuery.{matches, nested}
import zio.elasticsearch.query.{InnerHits, NestedQuery}

val nestedQuery: NestedQuery[Any] =
  nested(path = "subDocumentList", query = matches(field = "stringField", value = "test"))
    .innerHits(InnerHits().highlights(highlight(field = "stringField")))

val nestedRequest: SearchRequest = search(selectors = IndexName("index"), query = nestedQuery)
```

## Reading highlights

Highlighted snippets of each hit can be read from the `Item` using the `highlight` method (for a single field) or the `highlights` value (for all fields):
```scala
import zio._
import zio.elasticsearch.result.Item

val result: RIO[Elasticsearch, Chunk[Item]] =
  Elasticsearch.execute(request).flatMap(_.items)

val stringFieldHighlights: RIO[Elasticsearch, Chunk[Option[Chunk[String]]]] =
  result.map(_.map(_.highlight(field = Document.stringField)))

val allHighlights: RIO[Elasticsearch, Chunk[Option[Map[String, Chunk[String]]]]] =
  result.map(_.map(_.highlights))
```

Highlights of the inner hits can be read the same way, after getting the inner hits with the `innerHit` method (highlighted fields of the inner hits are keyed by their full path):
```scala
val innerHitsHighlights: RIO[Elasticsearch, Chunk[Option[Chunk[String]]]] =
  Elasticsearch
    .execute(nestedRequest)
    .flatMap(_.items)
    .map(_.flatMap(_.innerHit("subDocumentList")).flatten.map(_.highlight("subDocumentList.stringField")))
```

You can find more information about highlighting [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/highlighting.html).
