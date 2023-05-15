package zio.elasticsearch

import zio.Chunk
import zio.elasticsearch.ElasticAggregation.termsAggregation
import zio.elasticsearch.ElasticHighlight.highlight
import zio.elasticsearch.ElasticQuery.term
import zio.elasticsearch.ElasticRequest._
import zio.elasticsearch.ElasticSort.sortBy
import zio.elasticsearch.domain.{Location, TestDocument}
import zio.elasticsearch.highlights.{HighlightField, Highlights}
import zio.elasticsearch.query.sort.Missing.First
import zio.elasticsearch.query.sort.SortByFieldOptions
import zio.elasticsearch.request.Document
import zio.elasticsearch.request.UpdateConflicts.Abort
import zio.elasticsearch.script.Script
import zio.elasticsearch.utils.RichString
import zio.json.ast.Json
import zio.json.ast.Json.{Arr, Str}
import zio.test.Assertion.equalTo
import zio.test._

import java.time.LocalDate

object ElasticRequestDSLSpec extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment, Any] =
    suite("ElasticRequest")(
      suite("constructing")(
        test("aggregate") {
          val aggregateRequest = aggregate(index = Index, aggregation = Aggregation)

          assert(aggregateRequest)(equalTo(Aggregate(index = Index, aggregation = Aggregation)))
        },
        // todo bulk
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
        test("create without id") {
          val createRequest              = create(index = Index, doc = Doc1)
          val createRequestWithRefresh   = create(index = Index, doc = Doc1).refreshTrue
          val createRequestWithRouting   = create(index = Index, doc = Doc1).routing(RoutingValue)
          val createRequestWithAllParams = create(index = Index, doc = Doc1).refreshTrue.routing(RoutingValue)

          assert(createRequest)(
            equalTo(Create(index = Index, document = Document.from(Doc1), refresh = None, routing = None))
          ) && assert(createRequestWithRefresh)(
            equalTo(Create(index = Index, document = Document.from(Doc1), refresh = Some(true), routing = None))
          ) && assert(createRequestWithRouting)(
            equalTo(Create(index = Index, document = Document.from(Doc1), refresh = None, routing = Some(RoutingValue)))
          ) && assert(createRequestWithAllParams)(
            equalTo(
              Create(index = Index, document = Document.from(Doc1), refresh = Some(true), routing = Some(RoutingValue))
            )
          )
        },
        test("create with id") {
          val createRequest            = create(index = Index, id = DocId, doc = Doc1)
          val createRequestWithRefresh = create(index = Index, id = DocId, doc = Doc1).refreshTrue
          val createRequestWithRouting = create(index = Index, id = DocId, doc = Doc1).routing(RoutingValue)
          val createRequestWithAllParams =
            create(index = Index, id = DocId, doc = Doc1).refreshTrue.routing(RoutingValue)

          assert(createRequest)(
            equalTo(
              CreateWithId(
                index = Index,
                id = DocId,
                document = Document.from(Doc1),
                refresh = None,
                routing = None
              )
            )
          ) && assert(createRequestWithRefresh)(
            equalTo(
              CreateWithId(
                index = Index,
                id = DocId,
                document = Document.from(Doc1),
                refresh = Some(true),
                routing = None
              )
            )
          ) && assert(createRequestWithRouting)(
            equalTo(
              CreateWithId(
                index = Index,
                id = DocId,
                document = Document.from(Doc1),
                refresh = None,
                routing = Some(RoutingValue)
              )
            )
          ) && assert(createRequestWithAllParams)(
            equalTo(
              CreateWithId(
                index = Index,
                id = DocId,
                document = Document.from(Doc1),
                refresh = Some(true),
                routing = Some(RoutingValue)
              )
            )
          )
        },
        test("create index") {
          val createIndexRequest               = createIndex(Index)
          val createIndexRequestWithDefinition = createIndex(name = Index, definition = "definition")

          assert(createIndexRequest)(equalTo(CreateIndex(name = Index, definition = None))) &&
          assert(createIndexRequestWithDefinition)(equalTo(CreateIndex(name = Index, definition = Some("definition"))))
        },
        test("delete by id") {
          val deleteByIdRequest              = deleteById(index = Index, id = DocId)
          val deleteByIdRequestWithRefresh   = deleteById(index = Index, id = DocId).refreshTrue
          val deleteByIdRequestWithRouting   = deleteById(index = Index, id = DocId).routing(RoutingValue)
          val deleteByIdRequestWithAllParams = deleteById(index = Index, id = DocId).refreshTrue.routing(RoutingValue)

          assert(deleteByIdRequest)(
            equalTo(DeleteById(index = Index, id = DocId, refresh = None, routing = None))
          ) && assert(deleteByIdRequestWithRefresh)(
            equalTo(DeleteById(index = Index, id = DocId, refresh = Some(true), routing = None))
          ) && assert(deleteByIdRequestWithRouting)(
            equalTo(DeleteById(index = Index, id = DocId, refresh = None, routing = Some(RoutingValue)))
          ) && assert(deleteByIdRequestWithAllParams)(
            equalTo(DeleteById(index = Index, id = DocId, refresh = Some(true), routing = Some(RoutingValue)))
          )
        },
        test("delete by query") {
          val deleteByQueryRequest            = deleteByQuery(index = Index, query = Query)
          val deleteByQueryRequestWithRefresh = deleteByQuery(index = Index, query = Query).refreshTrue
          val deleteByQueryRequestWithRouting = deleteByQuery(index = Index, query = Query).routing(RoutingValue)
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
        test("delete index") {
          val deleteIndexRequest = deleteIndex(Index)

          assert(deleteIndexRequest)(equalTo(DeleteIndex(Index)))
        },
        test("exists") {
          val existsRequest            = exists(index = Index, id = DocId)
          val existsRequestWithRouting = exists(index = Index, id = DocId).routing(RoutingValue)

          assert(existsRequest)(equalTo(Exists(index = Index, id = DocId, routing = None))) &&
          assert(existsRequestWithRouting)(equalTo(Exists(index = Index, id = DocId, routing = Some(RoutingValue))))
        },
        test("get by id") {
          val getByIdRequest              = getById(index = Index, id = DocId)
          val getByIdRequestWithRefresh   = getById(index = Index, id = DocId).refreshTrue
          val getByIdRequestWithRouting   = getById(index = Index, id = DocId).routing(RoutingValue)
          val getByIdRequestWithAllParams = getById(index = Index, id = DocId).refreshTrue.routing(RoutingValue)

          assert(getByIdRequest)(equalTo(GetById(index = Index, id = DocId, refresh = None, routing = None))) && assert(
            getByIdRequestWithRefresh
          )(
            equalTo(GetById(index = Index, id = DocId, refresh = Some(true), routing = None))
          ) && assert(getByIdRequestWithRouting)(
            equalTo(GetById(index = Index, id = DocId, refresh = None, routing = Some(RoutingValue)))
          ) && assert(getByIdRequestWithAllParams)(
            equalTo(GetById(index = Index, id = DocId, refresh = Some(true), routing = Some(RoutingValue)))
          )
        },
        test("search") {
          val searchRequest         = search(index = Index, query = Query)
          val searchRequestWithSort = search(index = Index, query = Query).sort(sortBy(TestDocument.intField))
          val searchRequestWithSourceFiltering =
            search(index = Index, query = Query).includes("stringField", "doubleField").excludes("booleanField")
          val searchRequestWithFrom = search(index = Index, query = Query).from(5)
          val searchRequestWithHighlights =
            search(index = Index, query = Query).highlights(highlight(TestDocument.intField))
          val searchRequestWithRouting     = search(index = Index, query = Query).routing(RoutingValue)
          val searchRequestWithSearchAfter = search(index = Index, query = Query).searchAfter(Arr(Str("12345")))
          val searchRequestWithSize        = search(index = Index, query = Query).size(5)
          val searchRequestWithAllParams = search(index = Index, query = Query)
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
                index = Index,
                query = Query,
                sortBy = Chunk.empty,
                excluded = None,
                from = None,
                highlights = None,
                included = None,
                routing = None,
                searchAfter = None,
                size = None
              )
            )
          ) && assert(searchRequestWithSort)(
            equalTo(
              Search(
                index = Index,
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
                excluded = None,
                from = None,
                highlights = None,
                included = None,
                routing = None,
                searchAfter = None,
                size = None
              )
            )
          ) && assert(searchRequestWithSourceFiltering)(
            equalTo(
              Search(
                index = Index,
                query = Query,
                sortBy = Chunk.empty,
                excluded = Some(Chunk("booleanField")),
                from = None,
                highlights = None,
                included = Some(Chunk("stringField", "doubleField")),
                routing = None,
                searchAfter = None,
                size = None
              )
            )
          ) && assert(searchRequestWithFrom)(
            equalTo(
              Search(
                index = Index,
                query = Query,
                sortBy = Chunk.empty,
                excluded = None,
                from = Some(5),
                highlights = None,
                included = None,
                routing = None,
                searchAfter = None,
                size = None
              )
            )
          ) && assert(searchRequestWithHighlights)(
            equalTo(
              Search(
                index = Index,
                query = Query,
                sortBy = Chunk.empty,
                excluded = None,
                from = None,
                highlights = Some(
                  Highlights(fields = Chunk(HighlightField(field = "intField", config = Map.empty)), config = Map.empty)
                ),
                included = None,
                routing = None,
                searchAfter = None,
                size = None
              )
            )
          ) && assert(searchRequestWithRouting)(
            equalTo(
              Search(
                index = Index,
                query = Query,
                sortBy = Chunk.empty,
                excluded = None,
                from = None,
                highlights = None,
                included = None,
                routing = Some(RoutingValue),
                searchAfter = None,
                size = None
              )
            )
          ) && assert(searchRequestWithSearchAfter)(
            equalTo(
              Search(
                index = Index,
                query = Query,
                sortBy = Chunk.empty,
                excluded = None,
                from = None,
                highlights = None,
                included = None,
                routing = None,
                searchAfter = Some(Arr(Str("12345"))),
                size = None
              )
            )
          ) && assert(searchRequestWithSize)(
            equalTo(
              Search(
                index = Index,
                query = Query,
                sortBy = Chunk.empty,
                excluded = None,
                from = None,
                highlights = None,
                included = None,
                routing = None,
                searchAfter = None,
                size = Some(5)
              )
            )
          ) && assert(searchRequestWithAllParams)(
            equalTo(
              Search(
                index = Index,
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
                excluded = Some(Chunk("booleanField")),
                from = Some(5),
                highlights = Some(
                  Highlights(fields = Chunk(HighlightField(field = "intField", config = Map.empty)), config = Map.empty)
                ),
                included = Some(Chunk("stringField", "doubleField")),
                routing = Some(RoutingValue),
                searchAfter = Some(Arr(Str("12345"))),
                size = Some(5)
              )
            )
          )
        },
        test("search and aggregate") {
          val searchAndAggRequest = search(index = Index, query = Query, aggregation = Aggregation)
          val searchAndAggRequestWithSort =
            search(index = Index, query = Query, aggregation = Aggregation).sort(sortBy(TestDocument.intField))
          val searchAndAggRequestWithSourceFiltering =
            search(index = Index, query = Query, aggregation = Aggregation)
              .includes("stringField", "doubleField")
              .excludes("booleanField")
          val searchAndAggRequestWithFrom = search(index = Index, query = Query, aggregation = Aggregation).from(5)
          val searchAndAggRequestWithHighlights =
            search(index = Index, query = Query, aggregation = Aggregation).highlights(highlight(TestDocument.intField))
          val searchAndAggRequestWithRouting =
            search(index = Index, query = Query, aggregation = Aggregation).routing(RoutingValue)
          val searchAndAggRequestWithSearchAfter =
            search(index = Index, query = Query, aggregation = Aggregation).searchAfter(Arr(Str("12345")))
          val searchAndAggRequestWithSize = search(index = Index, query = Query, aggregation = Aggregation).size(5)
          val searchAndAggRequestWithAllParams = search(index = Index, query = Query, aggregation = Aggregation)
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
                index = Index,
                query = Query,
                aggregation = Aggregation,
                sortBy = Chunk.empty,
                excluded = None,
                from = None,
                highlights = None,
                included = None,
                routing = None,
                searchAfter = None,
                size = None
              )
            )
          ) && assert(searchAndAggRequestWithSort)(
            equalTo(
              SearchAndAggregate(
                index = Index,
                query = Query,
                aggregation = Aggregation,
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
                excluded = None,
                from = None,
                highlights = None,
                included = None,
                routing = None,
                searchAfter = None,
                size = None
              )
            )
          ) && assert(searchAndAggRequestWithSourceFiltering)(
            equalTo(
              SearchAndAggregate(
                index = Index,
                query = Query,
                aggregation = Aggregation,
                sortBy = Chunk.empty,
                excluded = Some(Chunk("booleanField")),
                from = None,
                highlights = None,
                included = Some(Chunk("stringField", "doubleField")),
                routing = None,
                searchAfter = None,
                size = None
              )
            )
          ) && assert(searchAndAggRequestWithFrom)(
            equalTo(
              SearchAndAggregate(
                index = Index,
                query = Query,
                aggregation = Aggregation,
                sortBy = Chunk.empty,
                excluded = None,
                from = Some(5),
                highlights = None,
                included = None,
                routing = None,
                searchAfter = None,
                size = None
              )
            )
          ) && assert(searchAndAggRequestWithHighlights)(
            equalTo(
              SearchAndAggregate(
                index = Index,
                query = Query,
                aggregation = Aggregation,
                sortBy = Chunk.empty,
                excluded = None,
                from = None,
                highlights = Some(
                  Highlights(fields = Chunk(HighlightField(field = "intField", config = Map.empty)), config = Map.empty)
                ),
                included = None,
                routing = None,
                searchAfter = None,
                size = None
              )
            )
          ) && assert(searchAndAggRequestWithRouting)(
            equalTo(
              SearchAndAggregate(
                index = Index,
                query = Query,
                aggregation = Aggregation,
                sortBy = Chunk.empty,
                excluded = None,
                from = None,
                highlights = None,
                included = None,
                routing = Some(RoutingValue),
                searchAfter = None,
                size = None
              )
            )
          ) && assert(searchAndAggRequestWithSearchAfter)(
            equalTo(
              SearchAndAggregate(
                index = Index,
                query = Query,
                aggregation = Aggregation,
                sortBy = Chunk.empty,
                excluded = None,
                from = None,
                highlights = None,
                included = None,
                routing = None,
                searchAfter = Some(Arr(Str("12345"))),
                size = None
              )
            )
          ) && assert(searchAndAggRequestWithSize)(
            equalTo(
              SearchAndAggregate(
                index = Index,
                query = Query,
                aggregation = Aggregation,
                sortBy = Chunk.empty,
                excluded = None,
                from = None,
                highlights = None,
                included = None,
                routing = None,
                searchAfter = None,
                size = Some(5)
              )
            )
          ) && assert(searchAndAggRequestWithAllParams)(
            equalTo(
              SearchAndAggregate(
                index = Index,
                query = Query,
                aggregation = Aggregation,
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
                excluded = Some(Chunk("booleanField")),
                from = Some(5),
                highlights = Some(
                  Highlights(fields = Chunk(HighlightField(field = "intField", config = Map.empty)), config = Map.empty)
                ),
                included = Some(Chunk("stringField", "doubleField")),
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
              Update(
                index = Index,
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
              Update(
                index = Index,
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
              Update(
                index = Index,
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
              Update(
                index = Index,
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
              Update(
                index = Index,
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
        test("update all by query") {
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
        test("update by query") {
          val updateByQueryRequest = updateByQuery(index = Index, query = Query, script = Script1)
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
        test("update by script") {
          val updateRequest            = updateByScript(Index, DocId, Script1)
          val updateRequestWithRefresh = updateByScript(Index, DocId, Script1).refreshTrue
          val updateRequestWithRouting = updateByScript(Index, DocId, Script1).routing(RoutingValue)
          val updateRequestWithUpsert  = updateByScript(Index, DocId, Script1).orCreate(Doc2)
          val updateRequestWithAllParams =
            updateByScript(Index, DocId, Script1).refreshTrue.routing(RoutingValue).orCreate(Doc2)

          assert(updateRequest)(
            equalTo(
              Update(
                index = Index,
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
              Update(
                index = Index,
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
              Update(
                index = Index,
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
              Update(
                index = Index,
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
              Update(
                index = Index,
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
          val upsertRequest            = upsert(index = Index, id = DocId, doc = Doc1)
          val upsertRequestWithRefresh = upsert(index = Index, id = DocId, doc = Doc1).refreshTrue
          val upsertRequestWithRouting = upsert(index = Index, id = DocId, doc = Doc1).routing(RoutingValue)
          val upsertRequestWithAllParams =
            upsert(index = Index, id = DocId, doc = Doc1).refreshTrue.routing(RoutingValue)

          assert(upsertRequest)(
            equalTo(
              CreateOrUpdate(index = Index, id = DocId, document = Document.from(Doc1), refresh = None, routing = None)
            )
          ) && assert(upsertRequestWithRefresh)(
            equalTo(
              CreateOrUpdate(
                index = Index,
                id = DocId,
                document = Document.from(Doc1),
                refresh = Some(true),
                routing = None
              )
            )
          ) && assert(upsertRequestWithRouting)(
            equalTo(
              CreateOrUpdate(
                index = Index,
                id = DocId,
                document = Document.from(Doc1),
                refresh = None,
                routing = Some(RoutingValue)
              )
            )
          ) && assert(upsertRequestWithAllParams)(
            equalTo(
              CreateOrUpdate(
                index = Index,
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
        test("successfully encode search request to JSON") {
          val jsonRequest: Json = search(Index, Query) match {
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

          assert(jsonRequest)(equalTo(expected.toJson))
        },
        test("successfully encode search request to JSON with search after parameter") {
          val jsonRequest: Json = search(Index, Query).searchAfter(Arr(Str("12345"))) match {
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
              |  },
              |  "search_after" : [
              |   "12345"
              |   ]
              |}
              |""".stripMargin

          assert(jsonRequest)(equalTo(expected.toJson))
        },
        test("successfully encode search request to JSON with size parameter") {
          val jsonRequest: Json = search(Index, Query).size(20) match {
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
              |  },
              |  "size" : 20
              |}
              |""".stripMargin

          assert(jsonRequest)(equalTo(expected.toJson))
        },
        test("successfully encode search request to JSON with excludes") {
          val jsonRequest: Json = search(Index, Query).excludes("subDocumentList") match {
            case r: ElasticRequest.Search => r.toJson
          }
          val expected =
            """
              |{
              |  "_source" : {
              |    "excludes" : [
              |      "subDocumentList"
              |    ]
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

          assert(jsonRequest)(equalTo(expected.toJson))
        },
        test("successfully encode search request to JSON with includes") {
          val jsonRequest: Json = search(Index, Query).includes("stringField", "doubleField") match {
            case r: ElasticRequest.Search => r.toJson
          }
          val expected =
            """
              |{
              |  "_source" : {
              |    "includes" : [
              |      "stringField",
              |      "doubleField"
              |    ]
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

          assert(jsonRequest)(equalTo(expected.toJson))
        },
        test("successfully encode search request to JSON with includes using a schema") {
          val jsonRequest: Json = search(Index, Query).includes[TestDocument] match {
            case r: ElasticRequest.Search => r.toJson
          }
          val expected =
            """
              |{
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
              |      "locationField.lat",
              |      "locationField.lon"
              |    ]
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

          assert(jsonRequest)(equalTo(expected.toJson))
        },
        test("successfully encode search request to JSON with multiple parameters") {
          val jsonRequest = search(Index, Query)
            .size(20)
            .sort(sortBy(TestDocument.intField).missing(First))
            .from(10) match {
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
              |  },
              |  "size" : 20,
              |  "from" : 10,
              |  "sort": [
              |    {
              |      "intField": {
              |        "missing": "_first"
              |      }
              |    }
              |  ]
              |}
              |""".stripMargin

          assert(jsonRequest)(equalTo(expected.toJson))
        },
        test("successfully encode search request to JSON with all parameters") {
          val jsonRequest = search(Index, Query)
            .size(20)
            .highlights(highlight(TestDocument.intField))
            .sort(sortBy(TestDocument.intField).missing(First))
            .from(10)
            .includes("stringField")
            .excludes("intField") match {
            case r: ElasticRequest.Search => r.toJson
          }
          val expected =
            """
              |{
              |  "_source" : {
              |    "includes" : [
              |      "stringField"
              |    ],
              |    "excludes" : [
              |      "intField"
              |    ]
              |  },
              |  "query" : {
              |    "range" : {
              |      "intField" : {
              |       "gte" : 10
              |      }
              |    }
              |  },
              |  "size" : 20,
              |  "from" : 10,
              |  "sort": [
              |    {
              |      "intField": {
              |        "missing": "_first"
              |      }
              |    }
              |  ],
              |  "highlight" : {
              |    "fields" : {
              |      "intField" : {}
              |    }
              |  }
              |}
              |""".stripMargin

          assert(jsonRequest)(equalTo(expected.toJson))
        },
        test("successfully encode search and aggregate request to JSON with all parameters") {
          val jsonRequest = search(Index, Query)
            .aggregate(termsAggregation(name = "aggregation", field = "day_of_week"))
            .size(20)
            .highlights(highlight(TestDocument.intField))
            .sort(sortBy(TestDocument.intField).missing(First))
            .from(10)
            .includes("stringField")
            .excludes("intField") match {
            case r: ElasticRequest.SearchAndAggregate => r.toJson
          }
          val expected =
            """
              |{
              |  "_source" : {
              |    "includes" : [
              |      "stringField"
              |    ],
              |    "excludes" : [
              |      "intField"
              |    ]
              |  },
              |  "query" : {
              |    "range" : {
              |      "intField" : {
              |       "gte" : 10
              |      }
              |    }
              |  },
              |  "size" : 20,
              |  "from" : 10,
              |  "sort": [
              |    {
              |      "intField": {
              |        "missing": "_first"
              |      }
              |    }
              |  ],
              |  "highlight" : {
              |    "fields" : {
              |      "intField" : {}
              |    }
              |  },
              |  "aggs": {
              |   "aggregation" : {
              |     "terms" : {
              |       "field" : "day_of_week"
              |     }
              |   }
              |  }
              |}
              |""".stripMargin

          assert(jsonRequest)(equalTo(expected.toJson))
        },
        test("successfully encode update by query request to JSON") {
          val jsonRequest = updateByQuery(
            index = Index,
            query = term(TestDocument.stringField.keyword, "StringField"),
            script = Script("ctx._source['intField']++")
          ) match { case r: UpdateByQuery => r.toJson }

          val expected =
            """
              |{
              |  "script": {
              |    "source": "ctx._source['intField']++"
              |  },
              |  "query": {
              |    "term": {
              |      "stringField.keyword": {
              |        "value": "StringField"
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(jsonRequest)(equalTo(expected.toJson))
        },
        test("successfully encode update request to JSON with all parameters - script") {
          val jsonRequest = updateByScript(
            index = Index,
            id = DocId,
            script = Script("ctx._source.intField += params['factor']").params("factor" -> 2)
          ).orCreate[TestDocument](
            TestDocument(
              stringField = "stringField",
              subDocumentList = Nil,
              dateField = LocalDate.parse("2020-10-10"),
              intField = 1,
              doubleField = 1.0,
              booleanField = true,
              locationField = Location(1.0, 1.0)
            )
          ) match { case r: Update => r.toJson }

          val expected =
            """
              |{
              |  "script": {
              |    "source": "ctx._source.intField += params['factor']",
              |    "params": {
              |      "factor": 2
              |    }
              |  },
              |  "upsert": {
              |    "stringField": "stringField",
              |    "subDocumentList": [],
              |    "dateField": "2020-10-10",
              |    "intField": 1,
              |    "doubleField": 1.0,
              |    "booleanField": true,
              |    "locationField" : {
              |      "lat" : 1.0,
              |      "lon" : 1.0
              |    }
              |  }
              |}
              |""".stripMargin

          assert(jsonRequest)(equalTo(expected.toJson))
        },
        test("successfully encode update request to JSON with all parameters - doc") {
          val jsonRequest = update[TestDocument](
            index = Index,
            id = DocId,
            doc = TestDocument(
              stringField = "stringField1",
              subDocumentList = Nil,
              dateField = LocalDate.parse("2020-10-10"),
              intField = 1,
              doubleField = 1.0,
              booleanField = true,
              locationField = Location(1.0, 1.0)
            )
          ).orCreate[TestDocument](
            TestDocument(
              stringField = "stringField2",
              subDocumentList = Nil,
              dateField = LocalDate.parse("2020-11-11"),
              intField = 2,
              doubleField = 2.0,
              booleanField = false,
              locationField = Location(1.0, 1.0)
            )
          ) match { case r: Update => r.toJson }

          val expected =
            """
              |{
              |  "doc": {
              |    "stringField": "stringField1",
              |    "subDocumentList": [],
              |    "dateField": "2020-10-10",
              |    "intField": 1,
              |    "doubleField": 1.0,
              |    "booleanField": true,
              |    "locationField" : {
              |      "lat" : 1.0,
              |      "lon" : 1.0
              |    }
              |  },
              |  "upsert": {
              |    "stringField": "stringField2",
              |    "subDocumentList": [],
              |    "dateField": "2020-11-11",
              |    "intField": 2,
              |    "doubleField": 2.0,
              |    "booleanField": false,
              |    "locationField" : {
              |      "lat" : 1.0,
              |      "lon" : 1.0
              |    }
              |  }
              |}
              |""".stripMargin

          assert(jsonRequest)(equalTo(expected.toJson))
        }
      )
    )

  private val Aggregation  = ElasticAggregation.maxAggregation(name = "aggregation", field = TestDocument.intField)
  private val Query        = ElasticQuery.range(TestDocument.intField).gte(10)
  private val Index        = IndexName("index")
  private val DocId        = DocumentId("documentid")
  private val RoutingValue = Routing("routing")

  private val Doc1 = TestDocument(
    stringField = "stringField1",
    subDocumentList = List(),
    dateField = LocalDate.parse("2020-10-10"),
    intField = 5,
    doubleField = 7.0,
    booleanField = true,
    locationField = Location(20.0, 21.0)
  )
  private val Doc2 = TestDocument(
    stringField = "stringField2",
    subDocumentList = List(),
    dateField = LocalDate.parse("2022-10-10"),
    intField = 10,
    doubleField = 17.0,
    booleanField = false,
    locationField = Location(10.0, 11.0)
  )
  private val Script1 = Script("doc['intField'].value * params['factor']").params("factor" -> 2)

}
