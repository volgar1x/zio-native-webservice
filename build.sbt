organization := "dev.zio.quickstart"
name         := "webservice"
scalaVersion := "3.3.1"

libraryDependencies ++= Seq(
  "com.h2database" % "h2"                % "2.2.222",
  "dev.zio"       %% "zio"               % "2.0.17",
  "dev.zio"       %% "zio-http"          % "3.0.0-RC2",
  "dev.zio"       %% "zio-json"          % "0.6.2",
  "dev.zio"       %% "zio-logging"       % "2.1.14",
  "dev.zio"       %% "zio-logging-slf4j" % "2.1.14",
  "io.getquill"   %% "quill-jdbc-zio"    % "4.6.0",
  "io.getquill"   %% "quill-zio"         % "4.6.0"
)

enablePlugins(JavaAppPackaging)

Compile / mainClass := Some("dev.volgar1x.quickstart.MainApp")

enablePlugins(GraalVMNativeImagePlugin)

graalVMNativeImageOptions ++= Seq(
  "--static",
  "--no-fallback",
  "--install-exit-handlers",
  "--enable-http",
  "--initialize-at-build-time=org.slf4j.LoggerFactory",
  "--initialize-at-build-time=org.slf4j.simple.SimpleLogger",
  "--initialize-at-build-time=org.slf4j.impl.StaticLoggerBinder",
  "--initialize-at-run-time=io.netty.util.internal.logging.Log4JLogger",
  "--initialize-at-run-time=io.netty.util.AbstractReferenceCounted",
  "--initialize-at-run-time=io.netty.channel.DefaultFileRegion",
  "--initialize-at-run-time=io.netty.channel.epoll",
  "--initialize-at-run-time=io.netty.channel.kqueue",
  "--initialize-at-run-time=io.netty.channel.unix",
  "--initialize-at-run-time=io.netty.handler.ssl",
  "--initialize-at-run-time=io.netty.incubator.channel.uring",
  "--allow-incomplete-classpath"
)

Compile / doc / sources := Seq.empty

semanticdbEnabled := true
semanticdbVersion := scalafixSemanticdb.revision
