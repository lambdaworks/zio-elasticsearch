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
import sttp.client4.testing.{ResponseStub, StubBody}
import sttp.client4.{GenericRequest, Response, StringBody}
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
import zio.{Chunk, Duration, Fiber, Task, TaskLayer, UIO, ZIO, ZLayer, durationInt}

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
        test("uses the sort of the request and ignores its size") {
          val request = ElasticRequest.search(index, matchAll).size(0).sort(sortBy("intField"))

          for {
            backend  <- RecordingBackend.make(pages = 1)
            items    <- runStream(backend, request, StreamConfig.Scroll)
            requests <- backend.requests
          } yield assert(pagesOf(items))(equalTo(Chunk(1))) &&
            assert(requests.head.body)(
              isSome(equalTo("""{ "query": { "match_all": {} }, "sort": ["intField"] }""".toJson))
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
        test("clears the scroll context when the stream is interrupted during the first request") {
          for {
            backend  <- RecordingBackend.make(pages = 5, firstPageDelay = Some(1.second))
            fiber    <- runStream(backend, ElasticRequest.search(index, matchAll), StreamConfig.Scroll).fork
            _        <- backend.requests.repeatUntil(_.nonEmpty)
            _        <- fiber.interruptFork
            exit     <- awaitWithTestClock(fiber)
            requests <- backend.requests
          } yield assert(exit.isInterrupted)(isTrue) &&
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
        },
        test("stops waiting for the scroll context to be cleared after a timeout") {
          for {
            backend  <- RecordingBackend.make(pages = 5, hangOnRelease = true)
            fiber    <- backend.stream(ElasticRequest.search(index, matchAll), StreamConfig.Scroll).take(1).runCollect.fork
            exit     <- awaitWithTestClock(fiber)
            requests <- backend.requests
          } yield assert(exit.toEither.map(pagesOf))(isRight(equalTo(Chunk(1)))) &&
            assert(requests.last.method)(equalTo(Method.DELETE))
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
        test("uses the sort and search_after of the request and ignores its size") {
          val request =
            ElasticRequest.search(index, matchAll).size(0).sort(sortBy("intField")).searchAfter(Arr(Num(10)))

          for {
            backend  <- RecordingBackend.make(pages = 1)
            items    <- runStream(backend, request, StreamConfig.SearchAfter)
            requests <- backend.requests
          } yield assert(pagesOf(items))(equalTo(Chunk(1))) &&
            assert(requests(1).body)(
              isSome(
                equalTo(
                  """
                    |{
                    |  "query": { "match_all": {} },
                    |  "sort": ["intField"],
                    |  "pit": { "id": "pit-0", "keep_alive": "1m" },
                    |  "search_after": [10]
                    |}
                    |""".stripMargin.toJson
                )
              )
            )
        },
        test("fails without opening a point in time when search_after is set on an unsorted request") {
          for {
            backend <- RecordingBackend.make(pages = 1)
            result  <-
              runStream(
                backend,
                ElasticRequest.search(index, matchAll).searchAfter(Arr(Num(10))),
                StreamConfig.SearchAfter
              ).exit
            requests <- backend.requests
          } yield assert(result)(
            fails(isSubtype[ElasticException](hasMessage(containsString("requires the request to be sorted"))))
          ) && assert(requests)(isEmpty)
        },
        test("keeps the previous point in time ID when the response doesn't contain one") {
          for {
            backend  <- RecordingBackend.make(pages = 2, omitPitId = true)
            items    <- runStream(backend, ElasticRequest.search(index, matchAll), StreamConfig.SearchAfter)
            requests <- backend.requests
          } yield assert(pagesOf(items))(equalTo(Chunk(1, 2))) &&
            assert(requests.slice(1, 4).map(_.body.flatMap(pitIdOf)))(
              equalTo(Chunk(Some("pit-0"), Some("pit-0"), Some("pit-0")))
            ) &&
            assert(requests.last.body)(isSome(equalTo(Obj("id" -> Str("pit-0")))))
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
        },
        test("stops waiting for the point in time to be closed after a timeout") {
          for {
            backend <- RecordingBackend.make(pages = 5, hangOnRelease = true)
            fiber   <-
              backend.stream(ElasticRequest.search(index, matchAll), StreamConfig.SearchAfter).take(1).runCollect.fork
            exit     <- awaitWithTestClock(fiber)
            requests <- backend.requests
          } yield assert(exit.toEither.map(pagesOf))(isRight(equalTo(Chunk(1)))) &&
            assert(requests.last.method)(equalTo(Method.DELETE))
        }
      )
    )

  private final case class RecordedRequest(method: Method, uri: String, body: Option[Json])

  /**
   * Stub of Elasticsearch which serves `pages` pages with a single hit each (followed by an empty page) for both
   * streaming modes and records every request it receives. It fails when serving `failingPage`, leaves out `pit_id`
   * from search responses when `omitPitId` is set, never responds to release requests when `hangOnRelease` is set, and
   * delays the first page by `firstPageDelay`.
   */
  private final class RecordingBackend(
    pages: Int,
    failingPage: Option[Int],
    omitPitId: Boolean,
    hangOnRelease: Boolean,
    firstPageDelay: Option[Duration]
  ) {
    private val recorded = new ConcurrentLinkedQueue[RecordedRequest]()
    private val served   = new AtomicInteger(0)

    def requests: UIO[Chunk[RecordedRequest]] =
      ZIO.succeed(Chunk.fromArray(recorded.toArray(Array.empty[RecordedRequest])))

    def stream(request: ElasticRequest.SearchRequest, config: StreamConfig): ZStream[Any, Throwable, Item] =
      Executor.stream(request, config).provideLayer(layer)

    private def respond(request: GenericRequest[_, _]): Task[Response[StubBody]] = {
      val body = request.body match {
        case StringBody(s, _, _) => s.fromJson[Json].toOption
        case _                   => None
      }
      recorded.add(RecordedRequest(request.method, request.uri.toString, body))

      (request.method, request.uri.path.lastOption) match {
        case (Method.POST, Some("_pit")) =>
          ZIO.succeed(ResponseStub.adjust(Obj("id" -> Str("pit-0")).toString))
        case (Method.DELETE, _) =>
          if (hangOnRelease) ZIO.never else ZIO.succeed(ResponseStub.adjust("""{"succeeded":true}"""))
        case (Method.POST, Some("_search") | Some("scroll")) =>
          val page     = served.incrementAndGet()
          val response =
            if (failingPage.contains(page)) {
              ResponseStub.adjust("""{"error":"failure"}""", StatusCode.InternalServerError)
            } else if (request.uri.path.contains("repositories") || request.uri.path.lastOption.contains("scroll")) {
              ResponseStub.adjust(searchResponse(page, Some("_scroll_id" -> s"scroll-$page")))
            } else {
              ResponseStub.adjust(searchResponse(page, Some("pit_id" -> s"pit-$page").filterNot(_ => omitPitId)))
            }

          firstPageDelay.filter(_ => page == 1).fold(ZIO.succeed(response))(ZIO.sleep(_).as(response))
        case _ =>
          ZIO.succeed(ResponseStub.adjust("", StatusCode.NotFound))
      }
    }

    private def searchResponse(page: Int, id: Option[(String, String)]): String = {
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
        Chunk.fromIterable(id.map { case (field, value) => field -> Str(value) }) ++
          Chunk(
            "took"      -> Num(1),
            "timed_out" -> Bool(false),
            "_shards"   -> Obj("total" -> Num(1), "successful" -> Num(1), "failed" -> Num(0)),
            "hits"      -> Obj("hits" -> Arr(hits))
          )
      ).toString
    }

    private val layer: TaskLayer[Executor] = {
      val client: SttpClient = HttpClientZioBackend.stub.whenAnyRequest.thenRespondF(request => respond(request))
      (ZLayer.succeed(client) ++ ZLayer.succeed(ElasticConfig.Default)) >>> ElasticExecutor.live
    }
  }

  private object RecordingBackend {
    def make(
      pages: Int,
      failingPage: Option[Int] = None,
      omitPitId: Boolean = false,
      hangOnRelease: Boolean = false,
      firstPageDelay: Option[Duration] = None
    ): UIO[RecordingBackend] =
      ZIO.succeed(new RecordingBackend(pages, failingPage, omitPitId, hangOnRelease, firstPageDelay))
  }

  private val index: IndexName = IndexName("repositories")

  private val url = "http://localhost:9200"

  /**
   * Advances the test clock until the fiber is done, so that delayed responses and timeouts get a chance to fire.
   */
  private def awaitWithTestClock[E, A](fiber: Fiber[E, A]) =
    (TestClock.adjust(1.second) *> fiber.poll).repeatUntil(_.isDefined) *> fiber.await

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
