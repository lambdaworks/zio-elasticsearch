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

package example.external.github

import example.GitHubRepo
import example.external.github.model.RepoResponse
import sttp.client3.{SttpBackend, UriContext, basicRequest}
import zio.json.DecoderOps
import zio.{RIO, Task, ZIO}

object RepoFetcher {

  def fetchAllByOrganization(
    organization: String,
    limit: Int = 100
  ): RIO[SttpBackend[Task, Any], List[GitHubRepo]] =
    for {
      client <- ZIO.service[SttpBackend[Task, Any]]
      req     = basicRequest.get(uri"https://api.github.com/orgs/$organization/repos?per_page=$limit")
      res    <- req.send(client)
    } yield res.body.toOption
      .map(_.fromJson[List[RepoResponse]].fold(_ => Nil, _.map(GitHubRepo.fromResponse).toList))
      .getOrElse(Nil)
}
