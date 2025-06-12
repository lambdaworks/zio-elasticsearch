package zio.elasticsearch

import zio.Chunk
import zio.elasticsearch.ElasticAggregation.termsAggregation
import zio.elasticsearch.ElasticHighlight.highlight
import zio.elasticsearch.ElasticRequest._
import zio.elasticsearch.ElasticSort.sortBy
import zio.elasticsearch.IndexSelector.IndexNameSyntax
import zio.elasticsearch.data.GeoPoint
import zio.elasticsearch.domain.TestDocument
import zio.elasticsearch.highlights.{HighlightField, Highlights}
import zio.elasticsearch.query.sort.Missing.First
import zio.elasticsearch.query.sort.SortByFieldOptions
import zio.elasticsearch.request.Document
import zio.elasticsearch.request.UpdateConflicts.Abort
import zio.elasticsearch.script.Script
import zio.elasticsearch.utils.RichString
import zio.json.ast.Json.{Arr, Str}
import zio.test.Assertion.equalTo
import zio.test._

import java.time.LocalDate

object ElasticRequestSpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment, Any] =
    suite("ElasticRequest")(
      suite("constructing")(
        test("aggregate") {
          val aggregateRequest = aggregate(selectors = Index, aggregation = MaxAggregation)

          assert(aggregateRequest)(equalTo(Aggregate(selectors = Index.toSelector, aggregation = MaxAggregation)))
        },
        test("bulk") {
          val bulkRequest            = bulk(create(index = Index, doc = Doc1), upsert(index = Index, id = DocId, doc = Doc2))
          val bulkRequestWithRefresh =
            bulk(create(index = Index, doc = Doc1), upsert(index = Index, id = DocId, doc = Doc2)).refreshTrue
          val bulkRequestWithRouting =
            bulk(create(index = Index, doc = Doc1), upsert(index = Index, id = DocId, doc = Doc2)).routing(RoutingValue)
          val bulkRequestWithAllParams =
            bulk(create(index = Index, doc = Doc1), upsert(index = Index, id = DocId, doc = Doc2)).refreshTrue
              .routing(RoutingValue)

          assert(bulkRequest)(
            equalTo(
              Bulk(
                requests = Chunk(
                  Create(index = Some(Index), document = Document.from(Doc1), refresh = None, routing = None),
                  CreateOrUpdate(
                    index = Some(Index),
                    id = DocId,
                    document = Document.from(Doc2),
                    refresh = None,
                    routing = None
                  )
                ),
                index = None,
                refresh = None,
                routing = None
              )
            )
          ) && assert(bulkRequestWithRefresh)(
            equalTo(
              Bulk(
                requests = Chunk(
                  Create(index = Some(Index), document = Document.from(Doc1), refresh = None, routing = None),
                  CreateOrUpdate(
                    index = Some(Index),
                    id = DocId,
                    document = Document.from(Doc2),
                    refresh = None,
                    routing = None
                  )
                ),
                index = None,
                refresh = Some(true),
                routing = None
              )
            )
          ) && assert(bulkRequestWithRouting)(
            equalTo(
              Bulk(
                requests = Chunk(
                  Create(index = Some(Index), document = Document.from(Doc1), refresh = None, routing = None),
                  CreateOrUpdate(
                    index = Some(Index),
                    id = DocId,
                    document = Document.from(Doc2),
                    refresh = None,
                    routing = None
                  )
                ),
                index = None,
                refresh = None,
                routing = Some(RoutingValue)
              )
            )
          ) && assert(bulkRequestWithAllParams)(
            equalTo(
              Bulk(
                requests = Chunk(
                  Create(index = Some(Index), document = Document.from(Doc1), refresh = None, routing = None),
                  CreateOrUpdate(
                    index = Some(Index),
                    id = DocId,
                    document = Document.from(Doc2),
                    refresh = None,
                    routing = None
                  )
                ),
                index = None,
                refresh = Some(true),
                routing = Some(RoutingValue)
              )
            )
          )
        },
        test("bulk with global index") {
          val bulkRequestWithGlobalIndex =
            bulk(Index, create(doc = Doc1), upsert(id = DocId, doc = Doc2))

          val bulkRequestWithMixedIndices =
            bulk(Index, create(doc = Doc1), upsert(index = Index2, id = DocId, doc = Doc2))

          val bulkRequestWithAllIndividualIndices =
            bulk(Index, create(index = Index2, doc = Doc1), upsert(index = Index2, id = DocId, doc = Doc2))

          val bulkRequestWithGlobalIndexAndRefresh =
            bulk(Index, create(doc = Doc1), upsert(id = DocId, doc = Doc2)).refreshTrue

          val bulkRequestWithGlobalIndexAndRouting =
            bulk(Index, create(doc = Doc1), upsert(id = DocId, doc = Doc2)).routing(RoutingValue)

          val bulkRequestWithGlobalIndexAndAllParams =
            bulk(Index, create(doc = Doc1), upsert(id = DocId, doc = Doc2)).refreshTrue.routing(RoutingValue)

          val bulkRequestWithOverlappingRefresh =
            bulk(Index, create(doc = Doc1).refreshFalse, upsert(id = DocId, doc = Doc2)).refreshTrue

          val bulkRequestWithOverlappingRouting =
            bulk(Index, create(doc = Doc1).routing(RoutingValue2), upsert(id = DocId, doc = Doc2)).routing(RoutingValue)

          assert(bulkRequestWithGlobalIndex)(
            equalTo(
              Bulk(
                requests = Chunk(
                  Create(index = None, document = Document.from(Doc1), refresh = None, routing = None),
                  CreateOrUpdate(
                    index = None,
                    id = DocId,
                    document = Document.from(Doc2),
                    refresh = None,
                    routing = None
                  )
                ),
                index = Some(Index),
                refresh = None,
                routing = None
              )
            )
          ) &&
          assert(bulkRequestWithMixedIndices)(
            equalTo(
              Bulk(
                requests = Chunk(
                  Create(index = None, document = Document.from(Doc1), refresh = None, routing = None),
                  CreateOrUpdate(
                    index = Some(Index2),
                    id = DocId,
                    document = Document.from(Doc2),
                    refresh = None,
                    routing = None
                  )
                ),
                index = Some(Index),
                refresh = None,
                routing = None
              )
            )
          ) &&
          assert(bulkRequestWithAllIndividualIndices)(
            equalTo(
              Bulk(
                requests = Chunk(
                  Create(index = Some(Index2), document = Document.from(Doc1), refresh = None, routing = None),
                  CreateOrUpdate(
                    index = Some(Index2),
                    id = DocId,
                    document = Document.from(Doc2),
                    refresh = None,
                    routing = None
                  )
                ),
                index = Some(Index),
                refresh = None,
                routing = None
              )
            )
          ) &&
          assert(bulkRequestWithGlobalIndexAndRefresh)(
            equalTo(
              Bulk(
                requests = Chunk(
                  Create(index = None, document = Document.from(Doc1), refresh = None, routing = None),
                  CreateOrUpdate(
                    index = None,
                    id = DocId,
                    document = Document.from(Doc2),
                    refresh = None,
                    routing = None
                  )
                ),
                index = Some(Index),
                refresh = Some(true),
                routing = None
              )
            )
          ) &&
          assert(bulkRequestWithGlobalIndexAndRouting)(
            equalTo(
              Bulk(
                requests = Chunk(
                  Create(index = None, document = Document.from(Doc1), refresh = None, routing = None),
                  CreateOrUpdate(
                    index = None,
                    id = DocId,
                    document = Document.from(Doc2),
                    refresh = None,
                    routing = None
                  )
                ),
                index = Some(Index),
                refresh = None,
                routing = Some(RoutingValue)
              )
            )
          ) &&
          assert(bulkRequestWithGlobalIndexAndAllParams)(
            equalTo(
              Bulk(
                requests = Chunk(
                  Create(index = None, document = Document.from(Doc1), refresh = None, routing = None),
                  CreateOrUpdate(
                    index = None,
                    id = DocId,
                    document = Document.from(Doc2),
                    refresh = None,
                    routing = None
                  )
                ),
                index = Some(Index),
                refresh = Some(true),
                routing = Some(RoutingValue)
              )
            )
          ) &&
          assert(bulkRequestWithOverlappingRefresh)(
            equalTo(
              Bulk(
                requests = Chunk(
                  Create(index = None, document = Document.from(Doc1), refresh = Some(false), routing = None),
                  CreateOrUpdate(
                    index = None,
                    id = DocId,
                    document = Document.from(Doc2),
                    refresh = None,
                    routing = None
                  )
                ),
                index = Some(Index),
                refresh = Some(true),
                routing = None
              )
            )
          ) &&
          assert(bulkRequestWithOverlappingRouting)(
            equalTo(
              Bulk(
                requests = Chunk(
                  Create(index = None, document = Document.from(Doc1), refresh = None, routing = Some(RoutingValue2)),
                  CreateOrUpdate(
                    index = None,
                    id = DocId,
                    document = Document.from(Doc2),
                    refresh = None,
                    routing = None
                  )
                ),
                index = Some(Index),
                refresh = None,
                routing = Some(RoutingValue)
              )
            )
          )
        },
        test("count") {
          val countRequest              = count(Index)
          val countRequestWithQuery     = count(Index, Query)
          val countRequestWithRouting   = count(Index).routing(RoutingValue)
          val countRequestWithAllParams = count(Index, Query).routing(RoutingValue)

          assert(countRequest)(equalTo(Count(index = Index, query = None, routing = None))) &&
          assert(countRequestWithQuery)(equalTo(Count(index = Index, query = Some(Query), routing = None))) &&
          assert(countRequestWithRouting)(equalTo(Count(index = Index, query = None, routing = Some(RoutingValue)))) &&
          assert(countRequestWithAllParams)(
            equalTo(Count(index = Index, query = Some(Query), routing = Some(RoutingValue)))
          )
        },
        test("create") {
          val createRequest              = create(index = Index, doc = Doc1)
          val createRequestWithRefresh   = create(index = Index, doc = Doc1).refreshTrue
          val createRequestWithRouting   = create(index = Index, doc = Doc1).routing(RoutingValue)
          val createRequestWithAllParams = create(index = Index, doc = Doc1).refreshTrue.routing(RoutingValue)

          assert(createRequest)(
            equalTo(
              Create[Executable](index = Some(Index), document = Document.from(Doc1), refresh = None, routing = None)
            )
          ) && assert(createRequestWithRefresh)(
            equalTo(
              Create[Executable](
                index = Some(Index),
                document = Document.from(Doc1),
                refresh = Some(true),
                routing = None
              )
            )
          ) && assert(createRequestWithRouting)(
            equalTo(
              Create[Executable](
                index = Some(Index),
                document = Document.from(Doc1),
                refresh = None,
                routing = Some(RoutingValue)
              )
            )
          ) && assert(createRequestWithAllParams)(
            equalTo(
              Create[Executable](
                index = Some(Index),
                document = Document.from(Doc1),
                refresh = Some(true),
                routing = Some(RoutingValue)
              )
            )
          )
        },
        test("create with ID") {
          val createRequest              = create(index = Index, id = DocId, doc = Doc1)
          val createRequestWithRefresh   = create(index = Index, id = DocId, doc = Doc1).refreshTrue
          val createRequestWithRouting   = create(index = Index, id = DocId, doc = Doc1).routing(RoutingValue)
          val createRequestWithAllParams =
            create(index = Index, id = DocId, doc = Doc1).refreshTrue.routing(RoutingValue)

          assert(createRequest)(
            equalTo(
              CreateWithId[Executable](
                index = Some(Index),
                id = DocId,
                document = Document.from(Doc1),
                refresh = None,
                routing = None
              )
            )
          ) && assert(createRequestWithRefresh)(
            equalTo(
              CreateWithId[Executable](
                index = Some(Index),
                id = DocId,
                document = Document.from(Doc1),
                refresh = Some(true),
                routing = None
              )
            )
          ) && assert(createRequestWithRouting)(
            equalTo(
              CreateWithId[Executable](
                index = Some(Index),
                id = DocId,
                document = Document.from(Doc1),
                refresh = None,
                routing = Some(RoutingValue)
              )
            )
          ) && assert(createRequestWithAllParams)(
            equalTo(
              CreateWithId[Executable](
                index = Some(Index),
                id = DocId,
                document = Document.from(Doc1),
                refresh = Some(true),
                routing = Some(RoutingValue)
              )
            )
          )
        },
        test("createIndex") {
          val createIndexRequest               = createIndex(Index)
          val createIndexRequestWithDefinition = createIndex(index = Index, definition = "definition")

          assert(createIndexRequest)(equalTo(CreateIndex(index = Index, definition = None))) &&
          assert(createIndexRequestWithDefinition)(equalTo(CreateIndex(index = Index, definition = Some("definition"))))
        },
        test("deleteById") {
          val deleteByIdRequest              = deleteById(index = Index, id = DocId)
          val deleteByIdRequestWithRefresh   = deleteById(index = Index, id = DocId).refreshTrue
          val deleteByIdRequestWithRouting   = deleteById(index = Index, id = DocId).routing(RoutingValue)
          val deleteByIdRequestWithAllParams = deleteById(index = Index, id = DocId).refreshTrue.routing(RoutingValue)

          assert(deleteByIdRequest)(
            equalTo(DeleteById[Executable](index = Some(Index), id = DocId, refresh = None, routing = None))
          ) && assert(deleteByIdRequestWithRefresh)(
            equalTo(DeleteById[Executable](index = Some(Index), id = DocId, refresh = Some(true), routing = None))
          ) && assert(deleteByIdRequestWithRouting)(
            equalTo(
              DeleteById[Executable](index = Some(Index), id = DocId, refresh = None, routing = Some(RoutingValue))
            )
          ) && assert(deleteByIdRequestWithAllParams)(
            equalTo(
              DeleteById[Executable](
                index = Some(Index),
                id = DocId,
                refresh = Some(true),
                routing = Some(RoutingValue)
              )
            )
          )
        },
        test("deleteByQuery") {
          val deleteByQueryRequest              = deleteByQuery(index = Index, query = Query)
          val deleteByQueryRequestWithRefresh   = deleteByQuery(index = Index, query = Query).refreshTrue
          val deleteByQueryRequestWithRouting   = deleteByQuery(index = Index, query = Query).routing(RoutingValue)
          val deleteByQueryRequestWithAllParams =
            deleteByQuery(index = Index, query = Query).refreshTrue.routing(RoutingValue)

          assert(deleteByQueryRequest)(
            equalTo(DeleteByQuery(index = Index, query = Query, refresh = None, routing = None))
          ) && assert(deleteByQueryRequestWithRefresh)(
            equalTo(DeleteByQuery(index = Index, query = Query, refresh = Some(true), routing = None))
          ) && assert(deleteByQueryRequestWithRouting)(
            equalTo(DeleteByQuery(index = Index, query = Query, refresh = None, routing = Some(RoutingValue)))
          ) && assert(deleteByQueryRequestWithAllParams)(
            equalTo(DeleteByQuery(index = Index, query = Query, refresh = Some(true), routing = Some(RoutingValue)))
          )
        },
        test("deleteIndex") {
          val deleteIndexRequest = deleteIndex(Index)

          assert(deleteIndexRequest)(equalTo(DeleteIndex(Index)))
        },
        test("exists") {
          val existsRequest            = exists(index = Index, id = DocId)
          val existsRequestWithRouting = exists(index = Index, id = DocId).routing(RoutingValue)

          assert(existsRequest)(equalTo(Exists(index = Index, id = DocId, routing = None))) &&
          assert(existsRequestWithRouting)(equalTo(Exists(index = Index, id = DocId, routing = Some(RoutingValue))))
        },
        test("getById") {
          val getByIdRequest              = getById(index = Index, id = DocId)
          val getByIdRequestWithRefresh   = getById(index = Index, id = DocId).refreshTrue
          val getByIdRequestWithRouting   = getById(index = Index, id = DocId).routing(RoutingValue)
          val getByIdRequestWithAllParams = getById(index = Index, id = DocId).refreshTrue.routing(RoutingValue)

          assert(getByIdRequest)(equalTo(GetById(index = Index, id = DocId, refresh = None, routing = None))) &&
          assert(getByIdRequestWithRefresh)(
            equalTo(GetById(index = Index, id = DocId, refresh = Some(true), routing = None))
          ) && assert(getByIdRequestWithRouting)(
            equalTo(GetById(index = Index, id = DocId, refresh = None, routing = Some(RoutingValue)))
          ) && assert(getByIdRequestWithAllParams)(
            equalTo(GetById(index = Index, id = DocId, refresh = Some(true), routing = Some(RoutingValue)))
          )
        },
        test("knnSearch") {
          val knnSearchRequest            = knnSearch(selectors = Index, query = KnnQuery)
          val knnSearchRequestWithFilter  = knnSearch(selectors = Index, query = KnnQuery).filter(query = Query)
          val knnSearchRequestWithRouting =
            knnSearch(selectors = Index, query = KnnQuery.similarity(3.14)).routing(RoutingValue)
          val knnSearchRequestWithAllParams =
            knnSearch(selectors = Index, query = KnnQuery).filter(query = Query).routing(RoutingValue)

          assert(knnSearchRequest)(
            equalTo(KNN(knn = KnnQuery, selectors = Index.toSelector, filter = None, routing = None))
          ) &&
          assert(knnSearchRequestWithFilter)(
            equalTo(KNN(knn = KnnQuery, selectors = Index.toSelector, filter = Some(Query), routing = None))
          ) &&
          assert(knnSearchRequestWithRouting)(
            equalTo(
              KNN(
                knn = KnnQuery.similarity(3.14),
                selectors = Index.toSelector,
                filter = None,
                routing = Some(RoutingValue)
              )
            )
          ) &&
          assert(knnSearchRequestWithAllParams)(
            equalTo(
              KNN(knn = KnnQuery, selectors = Index.toSelector, filter = Some(Query), routing = Some(RoutingValue))
            )
          )
        },
        test("refresh") {
          val refreshRequest        = refresh(Index)
          val refreshWithMultiIndex = refresh(Indices)
          assert(refreshRequest)(equalTo(Refresh(Index.toSelector))) &&
          assert(refreshWithMultiIndex)(equalTo(Refresh(Indices.toSelector)))
        },
        test("search") {
          val searchRequest                    = search(selectors = Index, query = Query)
          val searchRequestWithSort            = search(selectors = Index, query = Query).sort(sortBy(TestDocument.intField))
          val searchRequestWithSourceFiltering =
            search(selectors = Index, query = Query).includes("stringField", "doubleField").excludes("booleanField")
          val searchRequestWithFrom       = search(selectors = Index, query = Query).from(5)
          val searchRequestWithHighlights =
            search(selectors = Index, query = Query).highlights(highlight(TestDocument.intField))
          val searchRequestWithRouting     = search(selectors = Index, query = Query).routing(RoutingValue)
          val searchRequestWithSearchAfter = search(selectors = Index, query = Query).searchAfter(Arr(Str("12345")))
          val searchRequestWithSize        = search(selectors = Index, query = Query).size(5)
          val searchRequestWithAllParams   = search(selectors = Index, query = Query)
            .sort(sortBy("intField"))
            .includes("stringField", "doubleField")
            .excludes("booleanField")
            .from(5)
            .highlights(highlight("intField"))
            .routing(RoutingValue)
            .searchAfter(Arr(Str("12345")))
            .size(5)

          assert(searchRequest)(
            equalTo(
              Search(
                excluded = Chunk(),
                included = Chunk(),
                selectors = Index.toSelector,
                query = Query,
                sortBy = Chunk.empty,
                from = None,
                highlights = None,
                routing = None,
                searchAfter = None,
                size = None
              )
            )
          ) && assert(searchRequestWithSort)(
            equalTo(
              Search(
                excluded = Chunk(),
                included = Chunk(),
                selectors = Index.toSelector,
                query = Query,
                sortBy = Chunk(
                  SortByFieldOptions(
                    field = "intField",
                    format = None,
                    missing = None,
                    mode = None,
                    numericType = None,
                    order = None,
                    unmappedType = None
                  )
                ),
                from = None,
                highlights = None,
                routing = None,
                searchAfter = None,
                size = None
              )
            )
          ) && assert(searchRequestWithSourceFiltering)(
            equalTo(
              Search(
                excluded = Chunk("booleanField"),
                included = Chunk("stringField", "doubleField"),
                selectors = Index.toSelector,
                query = Query,
                sortBy = Chunk.empty,
                from = None,
                highlights = None,
                routing = None,
                searchAfter = None,
                size = None
              )
            )
          ) && assert(searchRequestWithFrom)(
            equalTo(
              Search(
                excluded = Chunk(),
                included = Chunk(),
                selectors = Index.toSelector,
                query = Query,
                sortBy = Chunk.empty,
                from = Some(5),
                highlights = None,
                routing = None,
                searchAfter = None,
                size = None
              )
            )
          ) && assert(searchRequestWithHighlights)(
            equalTo(
              Search(
                excluded = Chunk(),
                included = Chunk(),
                selectors = Index.toSelector,
                query = Query,
                sortBy = Chunk.empty,
                from = None,
                highlights = Some(
                  Highlights(fields = Chunk(HighlightField(field = "intField", config = Map.empty)), config = Map.empty)
                ),
                routing = None,
                searchAfter = None,
                size = None
              )
            )
          ) && assert(searchRequestWithRouting)(
            equalTo(
              Search(
                excluded = Chunk(),
                included = Chunk(),
                selectors = Index.toSelector,
                query = Query,
                sortBy = Chunk.empty,
                from = None,
                highlights = None,
                routing = Some(RoutingValue),
                searchAfter = None,
                size = None
              )
            )
          ) && assert(searchRequestWithSearchAfter)(
            equalTo(
              Search(
                excluded = Chunk(),
                included = Chunk(),
                selectors = Index.toSelector,
                query = Query,
                sortBy = Chunk.empty,
                from = None,
                highlights = None,
                routing = None,
                searchAfter = Some(Arr(Str("12345"))),
                size = None
              )
            )
          ) && assert(searchRequestWithSize)(
            equalTo(
              Search(
                excluded = Chunk(),
                included = Chunk(),
                selectors = Index.toSelector,
                query = Query,
                sortBy = Chunk.empty,
                from = None,
                highlights = None,
                routing = None,
                searchAfter = None,
                size = Some(5)
              )
            )
          ) && assert(searchRequestWithAllParams)(
            equalTo(
              Search(
                excluded = Chunk("booleanField"),
                included = Chunk("stringField", "doubleField"),
                selectors = Index.toSelector,
                query = Query,
                sortBy = Chunk(
                  SortByFieldOptions(
                    field = "intField",
                    format = None,
                    missing = None,
                    mode = None,
                    numericType = None,
                    order = None,
                    unmappedType = None
                  )
                ),
                from = Some(5),
                highlights = Some(
                  Highlights(fields = Chunk(HighlightField(field = "intField", config = Map.empty)), config = Map.empty)
                ),
                routing = Some(RoutingValue),
                searchAfter = Some(Arr(Str("12345"))),
                size = Some(5)
              )
            )
          )
        },
        test("search + aggregate") {
          val searchAndAggRequest         = search(selectors = Index, query = Query, aggregation = MaxAggregation)
          val searchAndAggRequestWithSort =
            search(selectors = Index, query = Query, aggregation = MaxAggregation).sort(sortBy(TestDocument.intField))
          val searchAndAggRequestWithSourceFiltering =
            search(selectors = Index, query = Query, aggregation = MaxAggregation)
              .includes("stringField", "doubleField")
              .excludes("booleanField")
          val searchAndAggRequestWithFrom =
            search(selectors = Index, query = Query, aggregation = MaxAggregation).from(5)
          val searchAndAggRequestWithHighlights =
            search(selectors = Index, query = Query, aggregation = MaxAggregation).highlights(
              highlight(TestDocument.intField)
            )
          val searchAndAggRequestWithRouting =
            search(selectors = Index, query = Query, aggregation = MaxAggregation).routing(RoutingValue)
          val searchAndAggRequestWithSearchAfter =
            search(selectors = Index, query = Query, aggregation = MaxAggregation).searchAfter(Arr(Str("12345")))
          val searchAndAggRequestWithSize =
            search(selectors = Index, query = Query, aggregation = MaxAggregation).size(5)
          val searchAndAggRequestWithAllParams = search(selectors = Index, query = Query, aggregation = MaxAggregation)
            .sort(sortBy("intField"))
            .includes("stringField", "doubleField")
            .excludes("booleanField")
            .from(5)
            .highlights(highlight("intField"))
            .routing(RoutingValue)
            .searchAfter(Arr(Str("12345")))
            .size(5)

          assert(searchAndAggRequest)(
            equalTo(
              SearchAndAggregate(
                aggregation = MaxAggregation,
                excluded = Chunk(),
                included = Chunk(),
                selectors = Index.toSelector,
                query = Query,
                sortBy = Chunk.empty,
                from = None,
                highlights = None,
                routing = None,
                searchAfter = None,
                size = None
              )
            )
          ) && assert(searchAndAggRequestWithSort)(
            equalTo(
              SearchAndAggregate(
                aggregation = MaxAggregation,
                excluded = Chunk(),
                included = Chunk(),
                selectors = Index.toSelector,
                query = Query,
                sortBy = Chunk(
                  SortByFieldOptions(
                    field = "intField",
                    format = None,
                    missing = None,
                    mode = None,
                    numericType = None,
                    order = None,
                    unmappedType = None
                  )
                ),
                from = None,
                highlights = None,
                routing = None,
                searchAfter = None,
                size = None
              )
            )
          ) && assert(searchAndAggRequestWithSourceFiltering)(
            equalTo(
              SearchAndAggregate(
                aggregation = MaxAggregation,
                excluded = Chunk("booleanField"),
                included = Chunk("stringField", "doubleField"),
                selectors = Index.toSelector,
                query = Query,
                sortBy = Chunk.empty,
                from = None,
                highlights = None,
                routing = None,
                searchAfter = None,
                size = None
              )
            )
          ) && assert(searchAndAggRequestWithFrom)(
            equalTo(
              SearchAndAggregate(
                aggregation = MaxAggregation,
                excluded = Chunk(),
                included = Chunk(),
                selectors = Index.toSelector,
                query = Query,
                sortBy = Chunk.empty,
                from = Some(5),
                highlights = None,
                routing = None,
                searchAfter = None,
                size = None
              )
            )
          ) && assert(searchAndAggRequestWithHighlights)(
            equalTo(
              SearchAndAggregate(
                aggregation = MaxAggregation,
                excluded = Chunk(),
                included = Chunk(),
                selectors = Index.toSelector,
                query = Query,
                sortBy = Chunk.empty,
                from = None,
                highlights = Some(
                  Highlights(fields = Chunk(HighlightField(field = "intField", config = Map.empty)), config = Map.empty)
                ),
                routing = None,
                searchAfter = None,
                size = None
              )
            )
          ) && assert(searchAndAggRequestWithRouting)(
            equalTo(
              SearchAndAggregate(
                aggregation = MaxAggregation,
                excluded = Chunk(),
                included = Chunk(),
                selectors = Index.toSelector,
                query = Query,
                sortBy = Chunk.empty,
                from = None,
                highlights = None,
                routing = Some(RoutingValue),
                searchAfter = None,
                size = None
              )
            )
          ) && assert(searchAndAggRequestWithSearchAfter)(
            equalTo(
              SearchAndAggregate(
                aggregation = MaxAggregation,
                excluded = Chunk(),
                included = Chunk(),
                selectors = Index.toSelector,
                query = Query,
                sortBy = Chunk.empty,
                from = None,
                highlights = None,
                routing = None,
                searchAfter = Some(Arr(Str("12345"))),
                size = None
              )
            )
          ) && assert(searchAndAggRequestWithSize)(
            equalTo(
              SearchAndAggregate(
                aggregation = MaxAggregation,
                excluded = Chunk(),
                included = Chunk(),
                selectors = Index.toSelector,
                query = Query,
                sortBy = Chunk.empty,
                from = None,
                highlights = None,
                routing = None,
                searchAfter = None,
                size = Some(5)
              )
            )
          ) && assert(searchAndAggRequestWithAllParams)(
            equalTo(
              SearchAndAggregate(
                aggregation = MaxAggregation,
                excluded = Chunk("booleanField"),
                included = Chunk("stringField", "doubleField"),
                selectors = Index.toSelector,
                query = Query,
                sortBy = Chunk(
                  SortByFieldOptions(
                    field = "intField",
                    format = None,
                    missing = None,
                    mode = None,
                    numericType = None,
                    order = None,
                    unmappedType = None
                  )
                ),
                from = Some(5),
                highlights = Some(
                  Highlights(fields = Chunk(HighlightField(field = "intField", config = Map.empty)), config = Map.empty)
                ),
                routing = Some(RoutingValue),
                searchAfter = Some(Arr(Str("12345"))),
                size = Some(5)
              )
            )
          )
        },
        test("update") {
          val updateRequest              = update(Index, DocId, Doc1)
          val updateRequestWithRefresh   = update(Index, DocId, Doc1).refreshTrue
          val updateRequestWithRouting   = update(Index, DocId, Doc1).routing(RoutingValue)
          val updateRequestWithUpsert    = update(Index, DocId, Doc1).orCreate(Doc2)
          val updateRequestWithAllParams = update(Index, DocId, Doc1).refreshTrue.routing(RoutingValue).orCreate(Doc2)

          assert(updateRequest)(
            equalTo(
              Update[Executable](
                index = Some(Index),
                id = DocId,
                doc = Some(Document.from(Doc1)),
                refresh = None,
                routing = None,
                script = None,
                upsert = None
              )
            )
          ) && assert(updateRequestWithRefresh)(
            equalTo(
              Update[Executable](
                index = Some(Index),
                id = DocId,
                doc = Some(Document.from(Doc1)),
                refresh = Some(true),
                routing = None,
                script = None,
                upsert = None
              )
            )
          ) && assert(updateRequestWithRouting)(
            equalTo(
              Update[Executable](
                index = Some(Index),
                id = DocId,
                doc = Some(Document.from(Doc1)),
                refresh = None,
                routing = Some(RoutingValue),
                script = None,
                upsert = None
              )
            )
          ) && assert(updateRequestWithUpsert)(
            equalTo(
              Update[Executable](
                index = Some(Index),
                id = DocId,
                doc = Some(Document.from(Doc1)),
                refresh = None,
                routing = None,
                script = None,
                upsert = Some(Document.from(Doc2))
              )
            )
          ) && assert(updateRequestWithAllParams)(
            equalTo(
              Update[Executable](
                index = Some(Index),
                id = DocId,
                doc = Some(Document.from(Doc1)),
                refresh = Some(true),
                routing = Some(RoutingValue),
                script = None,
                upsert = Some(Document.from(Doc2))
              )
            )
          )
        },
        test("updateAllByQuery") {
          val updateByQueryRequest              = updateAllByQuery(index = Index, script = Script1)
          val updateByQueryRequestWithConflicts = updateAllByQuery(index = Index, script = Script1).conflicts(Abort)
          val updateByQueryRequestWithRefresh   = updateAllByQuery(index = Index, script = Script1).refreshTrue
          val updateByQueryRequestWithRouting   = updateAllByQuery(index = Index, script = Script1).routing(RoutingValue)
          val updateByQueryRequestWithAllParams =
            updateAllByQuery(index = Index, script = Script1).conflicts(Abort).refreshTrue.routing(RoutingValue)

          assert(updateByQueryRequest)(
            equalTo(
              UpdateByQuery(
                index = Index,
                script = Script1,
                conflicts = None,
                query = None,
                refresh = None,
                routing = None
              )
            )
          ) && assert(updateByQueryRequestWithConflicts)(
            equalTo(
              UpdateByQuery(
                index = Index,
                script = Script1,
                conflicts = Some(Abort),
                query = None,
                refresh = None,
                routing = None
              )
            )
          ) && assert(updateByQueryRequestWithRefresh)(
            equalTo(
              UpdateByQuery(
                index = Index,
                script = Script1,
                conflicts = None,
                query = None,
                refresh = Some(true),
                routing = None
              )
            )
          ) && assert(updateByQueryRequestWithRouting)(
            equalTo(
              UpdateByQuery(
                index = Index,
                script = Script1,
                conflicts = None,
                query = None,
                refresh = None,
                routing = Some(RoutingValue)
              )
            )
          ) && assert(updateByQueryRequestWithAllParams)(
            equalTo(
              UpdateByQuery(
                index = Index,
                script = Script1,
                conflicts = Some(Abort),
                query = None,
                refresh = Some(true),
                routing = Some(RoutingValue)
              )
            )
          )
        },
        test("updateByQuery") {
          val updateByQueryRequest              = updateByQuery(index = Index, query = Query, script = Script1)
          val updateByQueryRequestWithConflicts =
            updateByQuery(index = Index, query = Query, script = Script1).conflicts(Abort)
          val updateByQueryRequestWithRefresh =
            updateByQuery(index = Index, query = Query, script = Script1).refreshTrue
          val updateByQueryRequestWithRouting =
            updateByQuery(index = Index, query = Query, script = Script1).routing(RoutingValue)
          val updateByQueryRequestWithAllParams =
            updateByQuery(index = Index, query = Query, script = Script1)
              .conflicts(Abort)
              .refreshTrue
              .routing(RoutingValue)

          assert(updateByQueryRequest)(
            equalTo(
              UpdateByQuery(
                index = Index,
                script = Script1,
                conflicts = None,
                query = Some(Query),
                refresh = None,
                routing = None
              )
            )
          ) && assert(updateByQueryRequestWithConflicts)(
            equalTo(
              UpdateByQuery(
                index = Index,
                script = Script1,
                conflicts = Some(Abort),
                query = Some(Query),
                refresh = None,
                routing = None
              )
            )
          ) && assert(updateByQueryRequestWithRefresh)(
            equalTo(
              UpdateByQuery(
                index = Index,
                script = Script1,
                conflicts = None,
                query = Some(Query),
                refresh = Some(true),
                routing = None
              )
            )
          ) && assert(updateByQueryRequestWithRouting)(
            equalTo(
              UpdateByQuery(
                index = Index,
                script = Script1,
                conflicts = None,
                query = Some(Query),
                refresh = None,
                routing = Some(RoutingValue)
              )
            )
          ) && assert(updateByQueryRequestWithAllParams)(
            equalTo(
              UpdateByQuery(
                index = Index,
                script = Script1,
                conflicts = Some(Abort),
                query = Some(Query),
                refresh = Some(true),
                routing = Some(RoutingValue)
              )
            )
          )
        },
        test("updateByScript") {
          val updateRequest              = updateByScript(Index, DocId, Script1)
          val updateRequestWithRefresh   = updateByScript(Index, DocId, Script1).refreshTrue
          val updateRequestWithRouting   = updateByScript(Index, DocId, Script1).routing(RoutingValue)
          val updateRequestWithUpsert    = updateByScript(Index, DocId, Script1).orCreate(Doc2)
          val updateRequestWithAllParams =
            updateByScript(Index, DocId, Script1).refreshTrue.routing(RoutingValue).orCreate(Doc2)

          assert(updateRequest)(
            equalTo(
              Update[Executable](
                index = Some(Index),
                id = DocId,
                doc = None,
                refresh = None,
                routing = None,
                script = Some(Script1),
                upsert = None
              )
            )
          ) && assert(updateRequestWithRefresh)(
            equalTo(
              Update[Executable](
                index = Some(Index),
                id = DocId,
                doc = None,
                refresh = Some(true),
                routing = None,
                script = Some(Script1),
                upsert = None
              )
            )
          ) && assert(updateRequestWithRouting)(
            equalTo(
              Update[Executable](
                index = Some(Index),
                id = DocId,
                doc = None,
                refresh = None,
                routing = Some(RoutingValue),
                script = Some(Script1),
                upsert = None
              )
            )
          ) && assert(updateRequestWithUpsert)(
            equalTo(
              Update[Executable](
                index = Some(Index),
                id = DocId,
                doc = None,
                refresh = None,
                routing = None,
                script = Some(Script1),
                upsert = Some(Document.from(Doc2))
              )
            )
          ) && assert(updateRequestWithAllParams)(
            equalTo(
              Update[Executable](
                index = Some(Index),
                id = DocId,
                doc = None,
                refresh = Some(true),
                routing = Some(RoutingValue),
                script = Some(Script1),
                upsert = Some(Document.from(Doc2))
              )
            )
          )
        },
        test("upsert") {
          val upsertRequest              = upsert(index = Index, id = DocId, doc = Doc1)
          val upsertRequestWithRefresh   = upsert(index = Index, id = DocId, doc = Doc1).refreshTrue
          val upsertRequestWithRouting   = upsert(index = Index, id = DocId, doc = Doc1).routing(RoutingValue)
          val upsertRequestWithAllParams =
            upsert(index = Index, id = DocId, doc = Doc1).refreshTrue.routing(RoutingValue)

          assert(upsertRequest)(
            equalTo(
              CreateOrUpdate[Executable](
                index = Some(Index),
                id = DocId,
                document = Document.from(Doc1),
                refresh = None,
                routing = None
              )
            )
          ) && assert(upsertRequestWithRefresh)(
            equalTo(
              CreateOrUpdate[Executable](
                index = Some(Index),
                id = DocId,
                document = Document.from(Doc1),
                refresh = Some(true),
                routing = None
              )
            )
          ) && assert(upsertRequestWithRouting)(
            equalTo(
              CreateOrUpdate[Executable](
                index = Some(Index),
                id = DocId,
                document = Document.from(Doc1),
                refresh = None,
                routing = Some(RoutingValue)
              )
            )
          ) && assert(upsertRequestWithAllParams)(
            equalTo(
              CreateOrUpdate[Executable](
                index = Some(Index),
                id = DocId,
                document = Document.from(Doc1),
                refresh = Some(true),
                routing = Some(RoutingValue)
              )
            )
          )
        }
      ),
      suite("encoding as JSON")(
        test("aggregate") {
          val jsonRequest = aggregate(Index, MaxAggregation) match {
            case r: ElasticRequest.Aggregate => r.toJson
          }

          val expected =
            """
              |{
              |  "aggs": {
              |    "aggregation" : {
              |      "max" : {
              |        "field" : "intField"
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(jsonRequest)(equalTo(expected.toJson))
        },
        test("bulk") {
          val requestBody = bulk(
            create(index = Index, doc = Doc1).routing(RoutingValue),
            upsert(index = Index, id = DocId, doc = Doc2)
          ) match {
            case r: Bulk => r.body
          }

          val expected =
            """|{ "create" : { "_index" : "index", "routing" : "routing" } }
               |{"stringField":"stringField1","subDocumentList":[],"dateField":"2020-10-10","intField":5,"doubleField":7.0,"booleanField":true,"geoPointField":{"lat":20.0,"lon":21.0},"vectorField":[]}
               |{ "index" : { "_index" : "index", "_id" : "documentid" } }
               |{"stringField":"stringField2","subDocumentList":[],"dateField":"2022-10-10","intField":10,"doubleField":17.0,"booleanField":false,"geoPointField":{"lat":10.0,"lon":11.0},"vectorField":[]}
               |""".stripMargin

          assert(requestBody)(equalTo(expected))
        },
        test("bulk with global index") {
          val doc1Json =
            """{"stringField":"stringField1","subDocumentList":[],"dateField":"2020-10-10","intField":5,"doubleField":7.0,"booleanField":true,"geoPointField":{"lat":20.0,"lon":21.0},"vectorField":[]}"""
          val doc2Json =
            """{"stringField":"stringField2","subDocumentList":[],"dateField":"2022-10-10","intField":10,"doubleField":17.0,"booleanField":false,"geoPointField":{"lat":10.0,"lon":11.0},"vectorField":[]}"""

          val bodyGlobalIndexOnly = bulk(Index, create(doc = Doc1), upsert(id = DocId, doc = Doc2)) match {
            case r: Bulk => r.body
          }
          val expectedGlobalIndexOnly =
            s"""|{ "create" : { "_index" : "index" } }
                |$doc1Json
                |{ "index" : { "_index" : "index", "_id" : "documentid" } }
                |$doc2Json
                |""".stripMargin

          val bodyMixedIndices = bulk(Index, create(doc = Doc1), upsert(index = Index2, id = DocId, doc = Doc2)) match {
            case r: Bulk => r.body
          }
          val expectedMixedIndices =
            s"""|{ "create" : { "_index" : "index" } }
                |$doc1Json
                |{ "index" : { "_index" : "index2", "_id" : "documentid" } }
                |$doc2Json
                |""".stripMargin

          val bodyAllIndividualIndices =
            bulk(Index, create(index = Index2, doc = Doc1), upsert(index = Index2, id = DocId, doc = Doc2)) match {
              case r: Bulk => r.body
            }
          val expectedAllIndividualIndices =
            s"""|{ "create" : { "_index" : "index2" } }
                |$doc1Json
                |{ "index" : { "_index" : "index2", "_id" : "documentid" } }
                |$doc2Json
                |""".stripMargin

          val bodyIndividualRoutingOverridesGlobal =
            bulk(Index, create(doc = Doc1).routing(RoutingValue2), upsert(id = DocId, doc = Doc2))
              .routing(RoutingValue) match { case r: Bulk => r.body }
          val expectedIndividualRoutingOverridesGlobal =
            s"""|{ "create" : { "_index" : "index", "routing" : "routing2" } }
                |$doc1Json
                |{ "index" : { "_index" : "index", "_id" : "documentid" } }
                |$doc2Json
                |""".stripMargin

          assert(bodyGlobalIndexOnly)(equalTo(expectedGlobalIndexOnly)) &&
          assert(bodyMixedIndices)(equalTo(expectedMixedIndices)) &&
          assert(bodyAllIndividualIndices)(equalTo(expectedAllIndividualIndices)) &&
          assert(bodyIndividualRoutingOverridesGlobal)(equalTo(expectedIndividualRoutingOverridesGlobal))
        },
        test("count") {
          val jsonRequest = count(Index) match {
            case r: Count => r.toJson
          }
          val jsonRequestWithQuery = count(Index, Query) match {
            case r: Count => r.toJson
          }

          val expected =
            "{}"
          val expectedWithQuery =
            """
              |{
              |  "query" : {
              |    "range" : {
              |      "intField" : {
              |       "gte" : 10
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(jsonRequest)(equalTo(expected.toJson)) &&
          assert(jsonRequestWithQuery)(equalTo(expectedWithQuery.toJson))
        },
        test("create") {
          val jsonRequest = create(Index, Doc1) match {
            case r: Create[Executable] => r.toJson
          }

          val expected =
            """
              |{
              |  "stringField": "stringField1",
              |  "subDocumentList": [],
              |  "dateField": "2020-10-10",
              |  "intField": 5,
              |  "doubleField": 7.0,
              |  "booleanField": true,
              |  "geoPointField": {
              |    "lat": 20.0,
              |    "lon": 21.0
              |  },
              |  "vectorField": []
              |}
              |""".stripMargin

          assert(jsonRequest)(equalTo(expected.toJson))
        },
        test("create with ID") {
          val jsonRequest = create(index = Index, id = DocId, doc = Doc1) match {
            case r: CreateWithId[Executable] => r.toJson
          }

          val expected =
            """
              |{
              |  "stringField": "stringField1",
              |  "subDocumentList": [],
              |  "dateField": "2020-10-10",
              |  "intField": 5,
              |  "doubleField": 7.0,
              |  "booleanField": true,
              |  "geoPointField": {
              |    "lat": 20.0,
              |    "lon": 21.0
              |  },
              |  "vectorField": []
              |}
              |""".stripMargin

          assert(jsonRequest)(equalTo(expected.toJson))
        },
        test("createIndex") {
          val definition =
            """
              |{
              |  "settings": {
              |    "number_of_shards": 3
              |  }
              |}
              |""".stripMargin
          val jsonRequest = createIndex(Index) match {
            case r: CreateIndex => r.toJson
          }
          val jsonRequestWithDefinition = createIndex(index = Index, definition = definition) match {
            case r: CreateIndex => r.toJson
          }

          assert(jsonRequest)(equalTo("")) &&
          assert(jsonRequestWithDefinition)(equalTo(definition))
        },
        test("deleteByQuery") {
          val jsonRequest = deleteByQuery(index = Index, query = Query) match {
            case r: DeleteByQuery => r.toJson
          }

          val expected =
            """
              |{
              |  "query" : {
              |    "range" : {
              |      "intField" : {
              |       "gte" : 10
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(jsonRequest)(equalTo(expected.toJson))
        },
        test("knnSearch") {
          val jsonRequest = knnSearch(selectors = Index, query = KnnQuery) match {
            case r: ElasticRequest.KNN => r.toJson
          }
          val jsonRequestWithFilter =
            knnSearch(selectors = Index, query = KnnQuery.similarity(3.14)).filter(query = Query) match {
              case r: ElasticRequest.KNN => r.toJson
            }

          val expected =
            """
              |{
              |  "knn": {
              |    "field": "stringField",
              |    "query_vector": [1.1, 3.3],
              |    "k": 10,
              |    "num_candidates": 21
              |  }
              |}
              |""".stripMargin

          val expectedWithFilter =
            """
              |{
              |  "knn": {
              |    "field": "stringField",
              |    "query_vector": [1.1, 3.3],
              |    "k": 10,
              |    "num_candidates": 21,
              |    "similarity": 3.14
              |  },
              |  "filter": {
              |    "range": {
              |      "intField": {
              |        "gte": 10
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(jsonRequest)(equalTo(expected.toJson)) && assert(jsonRequestWithFilter)(
            equalTo(expectedWithFilter.toJson)
          )
        },
        test("search") {
          val jsonRequest = search(Index, Query) match {
            case r: ElasticRequest.Search => r.toJson
          }
          val jsonRequestWithSearchAfter = search(Index, Query).searchAfter(Arr(Str("12345"))) match {
            case r: ElasticRequest.Search => r.toJson
          }
          val jsonRequestWithSize = search(Index, Query).size(20) match {
            case r: ElasticRequest.Search => r.toJson
          }
          val jsonRequestWithExcludes = search(Index, Query).excludes("subDocumentList") match {
            case r: ElasticRequest.Search => r.toJson
          }
          val jsonRequestWithIncludes = search(Index, Query).includes("stringField", "doubleField") match {
            case r: ElasticRequest.Search => r.toJson
          }
          val jsonRequestWithInclSchema = search(Index, Query).includes[TestDocument] match {
            case r: ElasticRequest.Search => r.toJson
          }
          val jsonRequestWithAllParams = search(Index, Query)
            .size(20)
            .sort(sortBy(TestDocument.intField).missing(First))
            .excludes("intField")
            .from(10)
            .highlights(highlight(TestDocument.intField))
            .includes("stringField")
            .searchAfter(Arr(Str("12345")))
            .size(20) match {
            case r: ElasticRequest.Search => r.toJson
          }

          val expected =
            """
              |{
              |  "query" : {
              |    "range" : {
              |      "intField" : {
              |       "gte" : 10
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithSearchAfter =
            """
              |{
              |  "query" : {
              |    "range" : {
              |      "intField" : {
              |       "gte" : 10
              |      }
              |    }
              |  },
              |  "search_after" : [
              |     "12345"
              |   ]
              |}
              |""".stripMargin

          val expectedWithSize =
            """
              |{
              |  "size" : 20,
              |  "query" : {
              |    "range" : {
              |      "intField" : {
              |       "gte" : 10
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithExcludes =
            """
              |{
              |  "query" : {
              |    "range" : {
              |      "intField" : {
              |       "gte" : 10
              |      }
              |    }
              |  },
              |  "_source" : {
              |    "excludes" : [
              |      "subDocumentList"
              |    ]
              |  }
              |}
              |""".stripMargin

          val expectedWithIncludes =
            """
              |{
              |  "query" : {
              |    "range" : {
              |      "intField" : {
              |       "gte" : 10
              |      }
              |    }
              |  },
              |  "_source" : {
              |    "includes" : [
              |      "stringField",
              |      "doubleField"
              |    ]
              |  }
              |}
              |""".stripMargin

          val expectedWithInclSchema =
            """
              |{
              |  "query" : {
              |    "range" : {
              |      "intField" : {
              |       "gte" : 10
              |      }
              |    }
              |  },
              |  "_source" : {
              |    "includes" : [
              |      "stringField",
              |      "subDocumentList.stringField",
              |      "subDocumentList.nestedField.stringField",
              |      "subDocumentList.nestedField.longField",
              |      "subDocumentList.intField",
              |      "subDocumentList.intFieldList",
              |      "dateField",
              |      "intField",
              |      "doubleField",
              |      "booleanField",
              |      "geoPointField.lat",
              |      "geoPointField.lon",
              |      "vectorField"
              |    ]
              |  }
              |}
              |""".stripMargin

          val expectedWithAllParams =
            """
              |{
              |  "from" : 10,
              |  "size" : 20,
              |  "highlight" : {
              |    "fields" : {
              |      "intField" : {}
              |    }
              |  },
              |  "sort": [
              |    {
              |      "intField": {
              |        "missing": "_first"
              |      }
              |    }
              |  ],
              |  "query" : {
              |    "range" : {
              |      "intField" : {
              |       "gte" : 10
              |      }
              |    }
              |  },
              |  "search_after" : [
              |     "12345"
              |  ],
              |  "_source" : {
              |    "includes" : [
              |      "stringField"
              |    ],
              |    "excludes" : [
              |      "intField"
              |    ]
              |  }
              |}
              |""".stripMargin

          assert(jsonRequest)(equalTo(expected.toJson)) &&
          assert(jsonRequestWithSearchAfter)(equalTo(expectedWithSearchAfter.toJson)) &&
          assert(jsonRequestWithSize)(equalTo(expectedWithSize.toJson)) &&
          assert(jsonRequestWithExcludes)(equalTo(expectedWithExcludes.toJson)) &&
          assert(jsonRequestWithIncludes)(equalTo(expectedWithIncludes.toJson)) &&
          assert(jsonRequestWithInclSchema)(equalTo(expectedWithInclSchema.toJson)) &&
          assert(jsonRequestWithAllParams)(equalTo(expectedWithAllParams.toJson))
        },
        test("search + aggregate") {
          val jsonRequest = search(selectors = Index, query = Query, aggregation = TermsAggregation) match {
            case r: SearchAndAggregate => r.toJson
          }
          val jsonRequestWithFrom =
            search(selectors = Index, query = Query, aggregation = TermsAggregation).from(10) match {
              case r: SearchAndAggregate => r.toJson
            }
          val jsonRequestWithSortAndHighlights =
            search(selectors = Index, query = Query, aggregation = TermsAggregation)
              .sort(sortBy(TestDocument.intField).missing(First))
              .highlights(highlight(TestDocument.intField)) match {
              case r: SearchAndAggregate => r.toJson
            }
          val jsonRequestWithAllParams = search(selectors = Index, query = Query)
            .aggregate(TermsAggregation)
            .from(10)
            .size(20)
            .highlights(highlight(TestDocument.intField))
            .sort(sortBy(TestDocument.intField).missing(First))
            .includes("stringField")
            .excludes("intField")
            .searchAfter(Arr(Str("12345"))) match {
            case r: ElasticRequest.SearchAndAggregate => r.toJson
          }

          val expected =
            """
              |{
              |  "query" : {
              |    "range" : {
              |      "intField" : {
              |        "gte" : 10
              |      }
              |    }
              |  },
              |  "aggs": {
              |    "aggregation" : {
              |      "terms" : {
              |        "field" : "intField"
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithFrom =
            """
              {
              |  "from": 10,
              |  "query" : {
              |    "range" : {
              |      "intField" : {
              |        "gte" : 10
              |      }
              |    }
              |  },
              |  "aggs": {
              |    "aggregation" : {
              |      "terms" : {
              |        "field" : "intField"
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithSortAndHighlights =
            """
              |{
              |  "highlight" : {
              |    "fields" : {
              |      "intField" : {}
              |    }
              |  },
              |  "sort": [
              |    {
              |      "intField": {
              |        "missing": "_first"
              |      }
              |    }
              |  ],
              |  "query" : {
              |    "range" : {
              |      "intField" : {
              |        "gte" : 10
              |      }
              |    }
              |  },
              |  "aggs": {
              |     "aggregation" : {
              |       "terms" : {
              |         "field" : "intField"
              |       }
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithAllParams =
            """
              |{
              |  "from" : 10,
              |  "size" : 20,
              |  "highlight" : {
              |    "fields" : {
              |      "intField" : {}
              |    }
              |  },
              |  "sort": [
              |    {
              |      "intField": {
              |        "missing": "_first"
              |      }
              |    }
              |  ],
              |  "query" : {
              |    "range" : {
              |      "intField" : {
              |        "gte" : 10
              |      }
              |    }
              |  },
              |  "aggs": {
              |   "aggregation" : {
              |     "terms" : {
              |       "field" : "intField"
              |     }
              |   }
              |  },
              |  "search_after" : [
              |    "12345"
              |  ],
              |  "_source" : {
              |    "includes" : [
              |      "stringField"
              |    ],
              |    "excludes" : [
              |      "intField"
              |    ]
              |  }
              |}
              |""".stripMargin

          assert(jsonRequest)(equalTo(expected.toJson)) &&
          assert(jsonRequestWithFrom)(equalTo(expectedWithFrom.toJson)) &&
          assert(jsonRequestWithSortAndHighlights)(equalTo(expectedWithSortAndHighlights.toJson)) &&
          assert(jsonRequestWithAllParams)(equalTo(expectedWithAllParams.toJson))
        },
        test("update") {
          val jsonRequest = update(index = Index, id = DocId, doc = Doc1) match {
            case r: Update[Executable] => r.toJson
          }
          val jsonRequestWithUpsert = update(index = Index, id = DocId, doc = Doc1).orCreate(Doc2) match {
            case r: Update[Executable] => r.toJson
          }

          val expected =
            """
              |{
              |  "doc": {
              |    "stringField": "stringField1",
              |    "subDocumentList": [],
              |    "dateField": "2020-10-10",
              |    "intField": 5,
              |    "doubleField": 7.0,
              |    "booleanField": true,
              |    "geoPointField": {
              |      "lat": 20.0,
              |      "lon": 21.0
              |    },
              |    "vectorField": []
              |  }
              |}
              |""".stripMargin

          val expectedWithUpsert =
            """
              |{
              |  "doc": {
              |    "stringField": "stringField1",
              |    "subDocumentList": [],
              |    "dateField": "2020-10-10",
              |    "intField": 5,
              |    "doubleField": 7.0,
              |    "booleanField": true,
              |    "geoPointField": {
              |      "lat": 20.0,
              |      "lon": 21.0
              |    },
              |    "vectorField": []
              |  },
              |  "upsert": {
              |    "stringField": "stringField2",
              |    "subDocumentList": [],
              |    "dateField": "2022-10-10",
              |    "intField": 10,
              |    "doubleField": 17.0,
              |    "booleanField": false,
              |    "geoPointField": {
              |      "lat": 10.0,
              |      "lon": 11.0
              |    },
              |    "vectorField": []
              |  }
              |}
              |""".stripMargin

          assert(jsonRequest)(equalTo(expected.toJson)) &&
          assert(jsonRequestWithUpsert)(equalTo(expectedWithUpsert.toJson))
        },
        test("updateByQuery") {
          val jsonRequest = updateAllByQuery(index = Index, script = Script1) match {
            case r: UpdateByQuery => r.toJson
          }
          val jsonRequestWithQuery = updateByQuery(index = Index, query = Query, script = Script1) match {
            case r: UpdateByQuery => r.toJson
          }

          val expected =
            """
              |{
              |  "script": {
              |    "source": "doc['intField'].value * params['factor']",
              |    "params": {
              |      "factor": 2
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithQuery =
            """
              |{
              |  "script": {
              |    "source": "doc['intField'].value * params['factor']",
              |    "params": {
              |      "factor": 2
              |    }
              |  },
              |  "query" : {
              |    "range" : {
              |      "intField" : {
              |       "gte" : 10
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(jsonRequest)(equalTo(expected.toJson)) &&
          assert(jsonRequestWithQuery)(equalTo(expectedWithQuery.toJson))
        },
        test("updateByScript") {
          val jsonRequest = updateByScript(index = Index, id = DocId, script = Script1) match {
            case r: Update[Executable] => r.toJson
          }
          val jsonRequestWithUpsert = updateByScript(index = Index, id = DocId, script = Script1).orCreate(Doc2) match {
            case r: Update[Executable] => r.toJson
          }

          val expected =
            """
              |{
              |  "script": {
              |    "source": "doc['intField'].value * params['factor']",
              |    "params": {
              |      "factor": 2
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithUpsert =
            """
              |{
              |  "script": {
              |    "source": "doc['intField'].value * params['factor']",
              |    "params": {
              |      "factor": 2
              |    }
              |  },
              |  "upsert": {
              |    "stringField": "stringField2",
              |    "subDocumentList": [],
              |    "dateField": "2022-10-10",
              |    "intField": 10,
              |    "doubleField": 17.0,
              |    "booleanField": false,
              |    "geoPointField": {
              |      "lat": 10.0,
              |      "lon": 11.0
              |    },
              |    "vectorField": []
              |  }
              |}
              |""".stripMargin

          assert(jsonRequest)(equalTo(expected.toJson)) &&
          assert(jsonRequestWithUpsert)(equalTo(expectedWithUpsert.toJson))
        },
        test("upsert") {
          val jsonRequest = upsert(index = Index, id = DocId, doc = Doc1) match {
            case r: CreateOrUpdate[Executable] => r.toJson
          }

          val expected =
            """
              |{
              |  "stringField": "stringField1",
              |  "subDocumentList": [],
              |  "dateField": "2020-10-10",
              |  "intField": 5,
              |  "doubleField": 7.0,
              |  "booleanField": true,
              |  "geoPointField": {
              |    "lat": 20.0,
              |    "lon": 21.0
              |  },
              |  "vectorField": []
              |}
              |""".stripMargin

          assert(jsonRequest)(equalTo(expected.toJson))
        }
      )
    )

  private val Doc1 = TestDocument(
    stringField = "stringField1",
    subDocumentList = List(),
    dateField = LocalDate.parse("2020-10-10"),
    intField = 5,
    doubleField = 7.0,
    booleanField = true,
    geoPointField = GeoPoint(20.0, 21.0),
    vectorField = List()
  )
  private val Doc2 = TestDocument(
    stringField = "stringField2",
    subDocumentList = List(),
    dateField = LocalDate.parse("2022-10-10"),
    intField = 10,
    doubleField = 17.0,
    booleanField = false,
    geoPointField = GeoPoint(10.0, 11.0),
    vectorField = List()
  )
  private val DocId            = DocumentId("documentid")
  private val Index            = IndexName("index")
  private val Index2           = IndexName("index2")
  private val MaxAggregation   = ElasticAggregation.maxAggregation(name = "aggregation", field = TestDocument.intField)
  private val Indices          = MultiIndex.names(Index, IndexName("index2"))
  private val Query            = ElasticQuery.range(TestDocument.intField).gte(10)
  private val KnnQuery         = ElasticQuery.kNN(TestDocument.stringField, 10, 21, Chunk(1.1, 3.3))
  private val RoutingValue     = Routing("routing")
  private val RoutingValue2    = Routing("routing2")
  private val Script1          = Script("doc['intField'].value * params['factor']").params("factor" -> 2)
  private val TermsAggregation = termsAggregation(name = "aggregation", field = "intField")
}
