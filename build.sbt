ThisBuild / version      := "1.0.0"
ThisBuild / scalaVersion := "3.3.1"
ThisBuild / scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation"
)

ThisBuild / testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")

val sttpVersion       = "3.8.8"
val tapirVersion      = "1.2.6"
val zioConfigVersion  = "3.0.7"
val zioLoggingVersion = "2.1.8"
val zioVersion        = "2.0.9"

val dependencies = Seq(
  "com.softwaremill.sttp.tapir"   %% "tapir-sttp-client"      % tapirVersion,
  "com.softwaremill.sttp.tapir"   %% "tapir-json-zio"         % tapirVersion,
  "com.softwaremill.sttp.client3" %% "zio"                    % sttpVersion,
  "com.softwaremill.sttp.tapir"   %% "tapir-zio"              % tapirVersion, // Brings in zio, zio-streams
  "com.softwaremill.sttp.tapir"   %% "tapir-zio-http-server"  % tapirVersion, // Brings in zhttp
  "com.softwaremill.sttp.tapir"   %% "tapir-sttp-stub-server" % tapirVersion % "test",
  "com.softwaremill.sttp.tapir"   %% "tapir-sttp-client"      % tapirVersion,
  "com.softwaremill.sttp.tapir"   %% "tapir-json-zio"         % tapirVersion,
  "com.softwaremill.sttp.client3" %% "zio"                    % sttpVersion,
  "dev.zio"                       %% "zio-logging"            % zioLoggingVersion,
  "dev.zio"                       %% "zio-logging-slf4j"      % zioLoggingVersion,
  "ch.qos.logback"                 % "logback-classic"        % "1.4.4",
  "dev.zio"                       %% "zio-test"               % zioVersion,
  "dev.zio"                       %% "zio-test-junit"         % zioVersion % "test",
  "dev.zio"                       %% "zio-test-sbt"           % zioVersion % "test",
  "dev.zio"                       %% "zio-test-magnolia"      % zioVersion % "test",
  "dev.zio"                       %% "zio-mock"               % "1.0.0-RC9" % "test",
  "dev.zio"                       %% "zio-config"             % zioConfigVersion,
  "dev.zio"                       %% "zio-config-magnolia"    % zioConfigVersion,
  "dev.zio"                       %% "zio-config-typesafe"    % zioConfigVersion
)

lazy val root = (project in file("."))
  .settings(
    name := "weather",
    libraryDependencies ++= dependencies,
    assembly / mainClass       := Some("com.misterjvm.weather.Application"),
    assembly / assemblyJarName := "weather.jar",
    assembly / assemblyMergeStrategy := {
      case x if x.endsWith("module-info.class")                     => MergeStrategy.discard
      case x if x.endsWith(".tasty")                                => MergeStrategy.first
      case x if x.contains("META-INF/io.netty.versions.properties") => MergeStrategy.first
      case PathList("deriving.conf")                                => MergeStrategy.concat
      case x =>
        val oldStrategy = (ThisBuild / assemblyMergeStrategy).value
        oldStrategy(x)
    }
  )
