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
