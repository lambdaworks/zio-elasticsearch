package example

import example.api.Repositories
import example.config.{AppConfig, HttpConfig}
import zio._
import zio.config.getConfig
import zio.http.Server

object Main extends ZIOAppDefault {

  override def run: Task[Unit] =
    (for {
      http  <- getConfig[HttpConfig]
      _     <- ZIO.logInfo(s"Starting an HTTP service on port: ${http.port}")
      routes = Repositories.Routes
      _     <- Server.serve(routes)
    } yield ()).provide(AppConfig.live, Server.default)
}
