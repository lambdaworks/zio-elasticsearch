/*
 * Copyright 2022 LambdaWorks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package zio.elasticsearch

import sttp.client4.httpclient.zio.{HttpClientZioBackend, SttpClient}
import sttp.client4.testing.ResponseStub
import sttp.client4.{GenericRequest, StringBody}
import sttp.model.{Method, StatusCode}
import zio.elasticsearch.ElasticHighlight.highlight
import zio.elasticsearch.ElasticQuery.matchAll
import zio.elasticsearch.ElasticSort.sortBy
import zio.elasticsearch.executor.Executor
import zio.elasticsearch.result.{ElasticException, Item}
import zio.elasticsearch.utils._
import zio.json.DecoderOps
import zio.json.ast.Json
import zio.json.ast.Json.{Arr, Bool, Num, Obj, Str}
import zio.stream.ZStream
import zio.test.Assertion._
import zio.test._
import zio.{Chunk, TaskLayer, UIO, ZIO, ZLayer}

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

object HttpExecutorStreamSpec extends ZIOSpecDefault {

  def spec: Spec[TestEnvironment, Any] =
    suite("HttpExecutor streaming")(
      suite("scroll")(
        test("sends the request options and clears the scroll context once completed") {
          val request =
            ElasticRequest
              .search(index, matchAll)
              .includes("intField")
              .excludes("stringField")
              .highlights(highlight("stringField"))
              .from(5)
              .size(3)

          for {
            backend  <- RecordingBackend.make(pages = 2)
            items    <- runStream(backend, request, StreamConfig.Scroll.withPageSize(2))
            requests <- backend.requests
          } yield assert(pagesOf(items))(equalTo(Chunk(1, 2))) &&
            assert(requests.map(r => (r.method, r.uri)))(
              equalTo(
                Chunk(
                  (Method.POST, s"$url/repositories/_search?scroll=1m"),
                  (Method.POST, s"$url/_search/scroll?scroll=1m"),
                  (Method.POST, s"$url/_search/scroll?scroll=1m"),
                  (Method.DELETE, s"$url/_search/scroll")
                )
              )
            ) &&
            assert(requests.head.body)(
              isSome(
                equalTo(
                  """
                    |{
                    |  "query": { "match_all": {} },
                    |  "size": 2,
                    |  "highlight": { "fields": { "stringField": {} } },
                    |  "sort": ["_doc"],
                    |  "_source": { "includes": ["intField"], "excludes": ["stringField"] }
                    |}
                    |""".stripMargin.toJson
                )
              )
            ) &&
            assert(requests.map(_.body).slice(1, 3))(
              equalTo(Chunk(Some(Obj("scroll_id" -> Str("scroll-1"))), Some(Obj("scroll_id" -> Str("scroll-2")))))
            ) &&
            assert(requests.last.body)(isSome(equalTo(Obj("scroll_id" -> Str("scroll-3")))))
        },
        test("uses the size and sort of the request when no page size is configured") {
          val request = ElasticRequest.search(index, matchAll).size(3).sort(sortBy("intField"))

          for {
            backend  <- RecordingBackend.make(pages = 1)
            _        <- runStream(backend, request, StreamConfig.Scroll)
            requests <- backend.requests
          } yield assert(requests.head.body)(
            isSome(equalTo("""{ "query": { "match_all": {} }, "size": 3, "sort": ["intField"] }""".toJson))
          )
        },
        test("clears the scroll context when the stream is terminated early") {
          for {
            backend  <- RecordingBackend.make(pages = 5)
            items    <- backend.stream(ElasticRequest.search(index, matchAll), StreamConfig.Scroll).take(1).runCollect
            requests <- backend.requests
          } yield assert(pagesOf(items))(equalTo(Chunk(1))) &&
            assert(requests.last.method)(equalTo(Method.DELETE)) &&
            assert(requests.last.body)(isSome(equalTo(Obj("scroll_id" -> Str("scroll-1")))))
        },
        test("clears the scroll context when the stream fails") {
          for {
            backend  <- RecordingBackend.make(pages = 5, failingPage = Some(2))
            result   <- runStream(backend, ElasticRequest.search(index, matchAll), StreamConfig.Scroll).exit
            requests <- backend.requests
          } yield assert(result)(fails(isSubtype[ElasticException](anything))) &&
            assert(requests.last.method)(equalTo(Method.DELETE)) &&
            assert(requests.last.body)(isSome(equalTo(Obj("scroll_id" -> Str("scroll-1")))))
        }
      ),
      suite("search_after")(
        test("sends the request options and closes the point in time once completed") {
          val request =
            ElasticRequest
              .search(index, matchAll)
              .includes("intField")
              .excludes("stringField")
              .highlights(highlight("stringField"))
              .from(5)
              .routing(Routing("routing"))

          for {
            backend  <- RecordingBackend.make(pages = 2)
            items    <- runStream(backend, request, StreamConfig.SearchAfter.withPageSize(2))
            requests <- backend.requests
          } yield assert(pagesOf(items))(equalTo(Chunk(1, 2))) &&
            assert(requests.map(r => (r.method, r.uri)))(
              equalTo(
                Chunk(
                  (Method.POST, s"$url/repositories/_pit?keep_alive=1m&routing=routing"),
                  (Method.POST, s"$url/_search"),
                  (Method.POST, s"$url/_search"),
                  (Method.POST, s"$url/_search"),
                  (Method.DELETE, s"$url/_pit")
                )
              )
            ) &&
            assert(requests(1).body)(
              isSome(
                equalTo(
                  """
                    |{
                    |  "query": { "match_all": {} },
                    |  "size": 2,
                    |  "highlight": { "fields": { "stringField": {} } },
                    |  "_source": { "includes": ["intField"], "excludes": ["stringField"] },
                    |  "pit": { "id": "pit-0", "keep_alive": "1m" },
                    |  "sort": ["_shard_doc"],
                    |  "track_total_hits": false
                    |}
                    |""".stripMargin.toJson
                )
              )
            ) &&
            assert(requests.slice(2, 4).map(_.body.flatMap(searchAfterOf)))(
              equalTo(Chunk(Some(Arr(Num(1))), Some(Arr(Num(2)))))
            ) &&
            assert(requests.slice(2, 4).map(_.body.flatMap(pitIdOf)))(equalTo(Chunk(Some("pit-1"), Some("pit-2")))) &&
            assert(requests.last.body)(isSome(equalTo(Obj("id" -> Str("pit-3")))))
        },
        test("uses the size, sort and search_after of the request") {
          val request =
            ElasticRequest.search(index, matchAll).size(3).sort(sortBy("intField")).searchAfter(Arr(Num(10)))

          for {
            backend  <- RecordingBackend.make(pages = 1)
            _        <- runStream(backend, request, StreamConfig.SearchAfter)
            requests <- backend.requests
          } yield assert(requests(1).body)(
            isSome(
              equalTo(
                """
                  |{
                  |  "query": { "match_all": {} },
                  |  "size": 3,
                  |  "sort": ["intField"],
                  |  "pit": { "id": "pit-0", "keep_alive": "1m" },
                  |  "search_after": [10]
                  |}
                  |""".stripMargin.toJson
              )
            )
          )
        },
        test("closes the point in time when the stream is terminated early") {
          for {
            backend <- RecordingBackend.make(pages = 5)
            items   <-
              backend.stream(ElasticRequest.search(index, matchAll), StreamConfig.SearchAfter).take(1).runCollect
            requests <- backend.requests
          } yield assert(pagesOf(items))(equalTo(Chunk(1))) &&
            assert(requests.last.method)(equalTo(Method.DELETE)) &&
            assert(requests.last.body)(isSome(equalTo(Obj("id" -> Str("pit-1")))))
        },
        test("closes the point in time when the stream fails") {
          for {
            backend  <- RecordingBackend.make(pages = 5, failingPage = Some(2))
            result   <- runStream(backend, ElasticRequest.search(index, matchAll), StreamConfig.SearchAfter).exit
            requests <- backend.requests
          } yield assert(result)(fails(isSubtype[ElasticException](anything))) &&
            assert(requests.last.method)(equalTo(Method.DELETE)) &&
            assert(requests.last.body)(isSome(equalTo(Obj("id" -> Str("pit-1")))))
        }
      )
    )

  private final case class RecordedRequest(method: Method, uri: String, body: Option[Json])

  /**
   * Stub of Elasticsearch which serves `pages` pages with a single hit each (followed by an empty page) for both
   * streaming modes, records every request it receives, and fails when serving `failingPage`.
   */
  private final class RecordingBackend(pages: Int, failingPage: Option[Int]) {
    private val recorded = new ConcurrentLinkedQueue[RecordedRequest]()
    private val served   = new AtomicInteger(0)

    def requests: UIO[Chunk[RecordedRequest]] =
      ZIO.succeed(Chunk.fromArray(recorded.toArray(Array.empty[RecordedRequest])))

    def stream(request: ElasticRequest.SearchRequest, config: StreamConfig): ZStream[Any, Throwable, Item] =
      Executor.stream(request, config).provideLayer(layer)

    private def respond(request: GenericRequest[_, _]) = {
      val body = request.body match {
        case StringBody(s, _, _) => s.fromJson[Json].toOption
        case _                   => None
      }
      recorded.add(RecordedRequest(request.method, request.uri.toString, body))

      (request.method, request.uri.path.lastOption) match {
        case (Method.POST, Some("_pit"))                     => ResponseStub.adjust(Obj("id" -> Str("pit-0")).toString)
        case (Method.DELETE, _)                              => ResponseStub.adjust("""{"succeeded":true}""")
        case (Method.POST, Some("_search") | Some("scroll")) =>
          val page = served.incrementAndGet()
          if (failingPage.contains(page)) {
            ResponseStub.adjust("""{"error":"failure"}""", StatusCode.InternalServerError)
          } else if (request.uri.path.contains("repositories") || request.uri.path.lastOption.contains("scroll")) {
            ResponseStub.adjust(searchResponse(page, idField = "_scroll_id", id = s"scroll-$page"))
          } else {
            ResponseStub.adjust(searchResponse(page, idField = "pit_id", id = s"pit-$page"))
          }
        case _ =>
          ResponseStub.adjust("", StatusCode.NotFound)
      }
    }

    private def searchResponse(page: Int, idField: String, id: String): String = {
      val hits =
        if (page > pages) Chunk.empty
        else
          Chunk(
            Obj(
              "_index"  -> Str("repositories"),
              "_id"     -> Str(page.toString),
              "_source" -> Obj("page" -> Num(page)),
              "sort"    -> Arr(Num(page))
            )
          )

      Obj(
        idField     -> Str(id),
        "took"      -> Num(1),
        "timed_out" -> Bool(false),
        "_shards"   -> Obj("total" -> Num(1), "successful" -> Num(1), "failed" -> Num(0)),
        "hits"      -> Obj("hits" -> Arr(hits))
      ).toString
    }

    private val layer: TaskLayer[Executor] = {
      val client: SttpClient = HttpClientZioBackend.stub.whenRequestMatchesPartial { case r => respond(r) }
      (ZLayer.succeed(client) ++ ZLayer.succeed(ElasticConfig.Default)) >>> ElasticExecutor.live
    }
  }

  private object RecordingBackend {
    def make(pages: Int, failingPage: Option[Int] = None): UIO[RecordingBackend] =
      ZIO.succeed(new RecordingBackend(pages, failingPage))
  }

  private val index: IndexName = IndexName("repositories")

  private val url = "http://localhost:9200"

  private def pagesOf(items: Chunk[Item]): Chunk[Int] =
    items.flatMap(_.raw match {
      case Obj(fields) => Chunk.fromIterable(fields.collectFirst { case ("page", Num(n)) => n.intValue })
      case _           => Chunk.empty
    })

  private def pitIdOf(body: Json): Option[String] =
    body match {
      case Obj(fields) =>
        fields.collectFirst { case ("pit", Obj(pit)) => pit.collectFirst { case ("id", Str(id)) => id } }.flatten
      case _ => None
    }

  private def runStream(backend: RecordingBackend, request: ElasticRequest.SearchRequest, config: StreamConfig) =
    backend.stream(request, config).runCollect

  private def searchAfterOf(body: Json): Option[Json] =
    body match {
      case Obj(fields) => fields.collectFirst { case ("search_after", searchAfter) => searchAfter }
      case _           => None
    }
}
