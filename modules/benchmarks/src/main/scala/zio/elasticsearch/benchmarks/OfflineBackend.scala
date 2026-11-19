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

package zio.elasticsearch.benchmarks

import sttp.client4.httpclient.zio.{HttpClientZioBackend, SttpClient}
import sttp.model.Method
import zio.schema.codec.JsonCodec
import zio.{Chunk, ULayer, ZLayer}

private[benchmarks] object OfflineBackend {
  def layer(documents: Chunk[TestDocument]): ULayer[SttpClient] =
    ZLayer.succeed(
      HttpClientZioBackend.stub
        .whenRequestMatches(r => r.method == Method.POST && r.uri.path.lastOption.contains("_search"))
        .thenRespondAdjust(searchResponse(documents))
        .whenRequestMatches(r => r.method == Method.POST && r.uri.path.lastOption.contains("_bulk"))
        .thenRespondAdjust(bulkResponse(documents))
        .whenRequestMatches(_.method == Method.GET)
        .thenRespondAdjust(getResponse(documents.head))
        .whenRequestMatches(_.method == Method.PUT)
        .thenRespondAdjust(IndexResponse)
    )

  private def source(document: TestDocument): String =
    JsonCodec.jsonEncoder(TestDocument.schema).encodeJson(document, None).toString

  private def searchResponse(documents: Chunk[TestDocument]): String = {
    val hits = documents.map { document =>
      s"""{"_index":"$Index","_id":"${document.id}","_score":1.0,"_source":${source(document)}}"""
    }

    s"""{"took":3,"timed_out":false,"_shards":{"total":1,"successful":1,"failed":0},""" +
      s""""hits":{"total":{"value":${documents.size},"relation":"eq"},"max_score":1.0,"hits":[${hits.mkString(
          ","
        )}]}}"""
  }

  private def getResponse(document: TestDocument): String =
    s"""{"_index":"$Index","_id":"${document.id}","_version":1,"found":true,"_source":${source(document)}}"""

  private def bulkResponse(documents: Chunk[TestDocument]): String = {
    val items = documents.map { document =>
      s"""{"index":{"_index":"$Index","_id":"${document.id}","_version":1,"result":"created","status":201}}"""
    }

    s"""{"took":3,"errors":false,"items":[${items.mkString(",")}]}"""
  }

  private final val Index = "benchmark"

  private final val IndexResponse =
    s"""{"_index":"$Index","_id":"benchmark-document","_version":1,"result":"updated"}"""
}
