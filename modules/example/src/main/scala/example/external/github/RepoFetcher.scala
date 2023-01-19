package example.external.github

import example.GitHubRepo
import example.external.github.model.RepoResponse
import sttp.client3.{SttpBackend, UriContext, basicRequest}
import zio.{RIO, Task, ZIO}
import zio.json.DecoderOps

object RepoFetcher {

  def fetchAllByOrganization(
    organization: String,
    limit: Int = 100
  ): RIO[SttpBackend[Task, Any], List[GitHubRepo]] =
    for {
      sttpClient <- ZIO.service[SttpBackend[Task, Any]]
      req         = basicRequest.get(uri"https://api.github.com/orgs/$organization/repos?per_page=$limit")
      res        <- req.send(sttpClient)
    } yield res.body.toOption
      .map(_.fromJson[Array[RepoResponse]].fold(_ => Nil, _.map(GitHubRepo.fromResponse).toList))
      .getOrElse(Nil)
}
