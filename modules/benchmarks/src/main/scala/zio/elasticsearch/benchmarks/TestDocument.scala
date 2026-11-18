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

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import zio.Chunk
import zio.schema.{DeriveSchema, Schema}

final case class TestDocument(id: String, name: String, description: String, rating: Int)

object TestDocument {
  implicit val schema: Schema.CaseClass4[String, String, String, Int, TestDocument] = DeriveSchema.gen[TestDocument]

  private[benchmarks] final val Mapper: JsonMapper = JsonMapper.builder().addModule(DefaultScalaModule).build()

  private[benchmarks] final val Single: TestDocument =
    TestDocument(id = "benchmark-document", name = "zio", description = "zio elasticsearch benchmark", rating = 5)

  private[benchmarks] final val Terms: Chunk[String] =
    Chunk("zio", "scala", "elasticsearch", "benchmark", "effect", "fiber", "stream", "schema", "query", "index")

  private[benchmarks] def generate(count: Int): Chunk[TestDocument] =
    Chunk.fromIterable(0 until count).map { i =>
      TestDocument(
        id = s"document-$i",
        name = s"name-$i",
        description = s"document $i about ${Terms(i % Terms.size)} and ${Terms((i * 7) % Terms.size)}",
        rating = i % 10
      )
    }
}
