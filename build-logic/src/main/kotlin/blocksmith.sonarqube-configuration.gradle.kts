/**
 * Plugin for applying the Blocksmith SonarQube configuration across submodules.
 */

plugins {
    id("org.sonarqube")
}

val testingModuleName = "testing"
val subprojects = rootProject.subprojects.filter { !it.name.endsWith(testingModuleName) }

private val currentGitBranch = providers.of(GitBranchValueSource::class) {}

sonar {
    properties {

        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.token", getEnvVariable("SONAR_TOKEN"))

        property("sonar.projectKey", "fulminazzo_blocksmith")
        property("sonar.organization", "fulminazzo")
        property("sonar.projectName", "blocksmith")
        property("sonar.branch.name", currentGitBranch.get())

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
            subprojects.joinToString(",") {
                "${it.layout.buildDirectory.get()}/reports/codenarc/test.xml"
            }
        )
        // JaCoCo
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            "${rootProject.layout.buildDirectory.get()}/reports/jacoco/jacocoAggregatedReport/jacocoAggregatedReport.xml"
        )
        // SpotBugs
        property(
            "sonar.java.spotbugs.reportPaths",
            subprojects.joinToString(",") {
                "${it.layout.buildDirectory.get()}/reports/spotbugs/main.xml"
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
                listOf(
                    "src/test",
                    "src/integrationTest",
                    "src/functionalTest"
                ).joinToString(",")
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
