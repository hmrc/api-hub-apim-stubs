import sbt._

object AppDependencies {

  private val bootstrapVersion = "9.7.0"
  private val hmrcMongoVersion = "2.4.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-30"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-play-30"         % hmrcMongoVersion,
    "io.swagger.parser.v3"     % "swagger-parser"             % "2.1.25"
    excludeAll(
      ExclusionRule("com.fasterxml.jackson.core", "jackson-databind"),
      ExclusionRule("com.fasterxml.jackson.core", "jackson-core"),
      ExclusionRule("com.fasterxml.jackson.core", "jackson-annotations"),
      ExclusionRule("com.fasterxml.jackson.datatype", "jackson-datatype-jsr310"),
      ExclusionRule("com.fasterxml.jackson.dataformat", "jackson-dataformat-yaml")
    ),
    "com.fasterxml.jackson.dataformat" % "jackson-dataformat-yaml" % "2.14.3"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"     % bootstrapVersion            % Test,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-30"    % hmrcMongoVersion            % Test,
  )

  val it: Seq[Nothing] = Seq.empty

}
