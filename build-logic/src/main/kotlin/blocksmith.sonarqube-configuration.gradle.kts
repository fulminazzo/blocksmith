/**
 * Plugin for applying the Blocksmith SonarQube configuration across submodules.
 */

plugins {
    id("org.sonarqube")
}

val testingModuleName = "testing"
val testSourceSets = listOf("test", "integrationTest", "functionalTest")

private val currentGitBranch = providers.of(GitBranchValueSource::class) {}

sonar {
    properties {

        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.token", getEnvVariable("SONAR_TOKEN"))

        property("sonar.projectKey", "fulminazzo_blocksmith")
        property("sonar.organization", "fulminazzo")
        property("sonar.projectName", "blocksmith")
        /**
         * Disabled for SonarQube open source policies allowing only default branch analysis.
         * It should be noted that any execution of this task will OVERRIDE any previous analysis results,
         * regardless of the branch. Therefore, the CI/CD should be configured to run only on the default branch.
         */
//        property("sonar.branch.name", currentGitBranch.get())

        property("sonar.language", "java")

        // Checkstyle
        property(
            "sonar.java.checkstyle.reportPaths",
            subprojects.joinToString(",") {
                "${it.layout.buildDirectory.get()}/reports/checkstyle/main.xml"
            }
        )
        // CodeNarc
        property(
            "sonar.groovy.codenarc.reportPaths",
            subprojects.joinToString(",") { subproject ->
                testSourceSets.joinToString(",") {
                    "${subproject.layout.buildDirectory.get()}/reports/codenarc/$it.xml"
                }
            }
        )
        // JaCoCo
        property(
            "sonar.coverage.jacoco.aggregateXmlReportPaths",
            "${rootProject.layout.buildDirectory.get()}/reports/jacoco/jacocoAggregatedReport/jacocoAggregatedReport.xml"
        )
        // SpotBugs
        property(
            "sonar.java.spotbugs.reportPaths",
            subprojects.joinToString(",") { subproject ->
                testSourceSets.joinToString(",") {
                    "${subproject.layout.buildDirectory.get()}/reports/spotbugs/$it.xml"
                }
            }
        )
    }
}

subprojects.forEach { project ->
    project.sonar {
        properties {
            property("sonar.sources", "src/main")
            property(
                "sonar.tests",
                testSourceSets
                    .map { "src/$it" }
                    .filter { project.file(it).isDirectory }
                    .joinToString(",")
            )
        }
    }
}

private fun getEnvVariable(key: String): String =
    System.getenv(key) ?: rootProject.file(".env")
        .takeIf { it.exists() }
        ?.readLines()
        ?.firstOrNull { it.startsWith("$key=") }
        ?.split("=", limit = 2)[1]
    ?: throw GradleException("Missing environment variable: $key")

abstract class GitBranchValueSource : ValueSource<String, ValueSourceParameters.None> {
    override fun obtain(): String =
        ProcessBuilder("git", "rev-parse", "--abbrev-ref", "HEAD")
            .directory(File("."))
            .start()
            .inputStream.bufferedReader().readLine()
            ?.trim() ?: "main"
}
