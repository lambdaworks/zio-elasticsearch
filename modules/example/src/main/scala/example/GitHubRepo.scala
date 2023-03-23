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

package example

import example.external.github.model.RepoResponse
import zio.elasticsearch.FieldAccessorBuilder
import zio.json.{DeriveJsonEncoder, JsonEncoder}
import zio.schema.{DeriveSchema, Schema}

import java.time.{Instant, LocalDateTime, ZoneId}

final case class GitHubRepo(
  id: String,
  organization: String,
  name: String,
  url: String,
  description: Option[String],
  lastCommitAt: LocalDateTime,
  stars: Int,
  forks: Int
)

object GitHubRepo {
  def fromResponse(response: RepoResponse): GitHubRepo =
    GitHubRepo(
      id = response.id.toString,
      organization = response.owner.organization,
      name = response.name,
      url = response.url,
      description = response.description,
      lastCommitAt = LocalDateTime.ofInstant(Instant.parse(response.updatedAt), ZoneId.systemDefault()),
      stars = response.stars,
      forks = response.forks
    )

  implicit val schema
    : Schema.CaseClass8[String, String, String, String, Option[String], LocalDateTime, Int, Int, GitHubRepo] =
    DeriveSchema.gen[GitHubRepo]

  val (id, organization, name, url, description, lastCommitAt, stars, forks) =
    schema.makeAccessors(FieldAccessorBuilder)

  implicit val encoder: JsonEncoder[GitHubRepo] = DeriveJsonEncoder.gen[GitHubRepo]
}
