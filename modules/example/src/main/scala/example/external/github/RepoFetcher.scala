package example.external.github

import example.GitHubRepo
import example.external.github.model.RepoResponse
import zio.json.DecoderOps

import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.nio.charset.StandardCharsets

object RepoFetcher {

  def fetchAllByOrganization(organization: String): List[GitHubRepo] = {
    val httpClient: HttpClient = HttpClient.newHttpClient

    val request: HttpRequest =
      HttpRequest.newBuilder
        .uri(URI.create(s"https://api.github.com/orgs/$organization/repos?type=all&per_page=100"))
        .GET
        .build
    val response: HttpResponse[String] =
      httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))

    response.body.fromJson[Array[RepoResponse]].fold(_ => List(), _.map(GitHubRepo.fromResponse).toList)
  }
}
