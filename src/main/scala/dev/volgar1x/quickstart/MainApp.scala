package dev.volgar1x.quickstart

import dev.volgar1x.quickstart.counter.CounterApp
import dev.volgar1x.quickstart.download.DownloadApp
import dev.volgar1x.quickstart.greet.GreetingApp
import dev.volgar1x.quickstart.users.{
  InmemoryUserRepo,
  PersistentUserRepo,
  UserApp
}
import zio._
import zio.http._
import zio.logging._

object MainApp extends ZIOAppDefault:
  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    Runtime.removeDefaultLoggers >>> consoleLogger(
      ConsoleLoggerConfig(
        LogFormat.default |-| LogFormat.allAnnotations,
        LogFilter.logLevel(LogLevel.Info)
      )
    )

  def run: ZIO[Environment with ZIOAppArgs with Scope, Throwable, Any] =
    val httpApps = GreetingApp() ++ DownloadApp() ++ CounterApp() ++ UserApp()
    for
      _ <- ZIO.logInfo("Starting WebService...")
      _ <- (Server
        .install(
          (httpApps @@ HttpAppMiddleware
            .cors()).withDefaultErrorResponse @@ CustomMiddlewares.around {
            (request, response, responseTime) =>
              (
                ZIO.logDebug(s"Completed in ${responseTime.toMillis()}ms")
                  @@ CustomLog.status(response.status)
                  @@ CustomLog.method(request.method)
                  @@ CustomLog.location(request.url)
              )
          }
        )
        .flatMap { port => ZIO.logInfo(s"WebService started on port ${port}") }
        *> ZIO.never)
        .provide(
          Server.defaultWithPort(8080),

          // An layer responsible for storing the state of the `counterApp`
          ZLayer.fromZIO(Ref.make(0)),

          // To use the persistence layer, provide the `PersistentUserRepo.layer` layer instead
          InmemoryUserRepo.layer
        )
    yield ExitCode.success

  object CustomLog:
    import zio.logging.LogAnnotation

    val status =
      LogAnnotation[Status]("status", (a, b) => b, _.code.toString)
    val method =
      LogAnnotation[Method]("method", (a, b) => b, _.toString)
    val location =
      LogAnnotation[URL]("location", (a, b) => b, _.encode)

  object CustomMiddlewares:
    def around[R, E, A](f: (Request, Response, Duration) => ZIO[R, E, A]) =
      new RequestHandlerMiddleware.Simple[R, E] {

        override def apply[Env <: R, Err >: E](
            handler: Handler[Env, Err, Request, Response]
        )(implicit trace: Trace): Handler[Env, Err, Request, Response] =
          Handler.fromFunctionZIO { (request) =>
            handler
              .runZIO(request)
              .timed
              .tap { (responseTime, response) =>
                f(request, response, responseTime)
              }
              .map(_._2)
          }
      }

