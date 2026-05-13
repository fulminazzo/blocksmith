/**
 * Plugin for applying the Blocksmith SonarQube configuration across submodules.
 */

plugins {
    id("org.sonarqube")
}

val testingModuleName = "testing"

private val mainLanguages = listOf("groovy", "java", "kotlin")
private val testSourceSets = listOf("test", "integrationTest", "functionalTest")

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

        // Checkstyle
        property(
            "sonar.java.checkstyle.reportPaths",
            subprojects.joinToString(",") { it.getAllReportPaths("checkstyle") }
        )
        // CodeNarc
        property(
            "sonar.groovy.codenarc.reportPaths",
            subprojects.joinToString(",") { it.getAllReportPaths("codenarc") }
        )
        // JaCoCo
        property(
            "sonar.coverage.jacoco.aggregateXmlReportPaths",
            "${rootProject.layout.buildDirectory.get()}/reports/jacoco/jacocoAggregatedReport/jacocoAggregatedReport.xml"
        )
        // SpotBugs
        property(
            "sonar.java.spotbugs.reportPaths",
            subprojects.joinToString(",") { it.getAllReportPaths("spotbugs") }
        )
    }
}

subprojects.forEach { project ->
    project.sonar {
        properties {
            property("sonar.sources", project.getSourceSetPaths("main").joinToString(","))
            property("sonar.java.binaries", project.getSourceSetBuildPaths("main").joinToString(","))
            property("sonar.tests", project.testSourceSetsPaths.joinToString(","))
            property("sonar.java.test.binaries", project.testSourceSetsBuildPaths.joinToString(","))
        }
    }
}

private fun Project.getAllReportPaths(reportName: String): String =
    getReportPath("main", reportName) + "," + getTestReportPaths(reportName)

private fun Project.getTestReportPaths(reportName: String): String =
    testSourceSets.joinToString(",") { getReportPath(it, reportName) }

private fun Project.getReportPath(sourceSet: String, reportName: String): String =
    "${layout.buildDirectory.get()}/reports/$reportName/$sourceSet.xml"

private val Project.testSourceSetsPaths: List<String>
    get() = testSourceSets.flatMap { getSourceSetPaths(it) }

private fun Project.getSourceSetPaths(sourceSet: String): List<String> =
    mainLanguages
        .map { "src/$sourceSet/$it" }
        .filter { file(it).isDirectory }
        .filter { file(it).listFiles().isNotEmpty() }

private val Project.testSourceSetsBuildPaths: List<String>
    get() = testSourceSets.flatMap { getSourceSetBuildPaths(it) }

private fun Project.getSourceSetBuildPaths(sourceSet: String): List<String> =
    mainLanguages
        .map { "${layout.buildDirectory.get()}/classes/$it/$sourceSet" }
        .filter { file(it).isDirectory }
        .filter { file(it).listFiles().isNotEmpty() }

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
