/**
 * Plugin for applying the Blocksmith Spotbugs configuration across submodules.
 */

plugins {
    id("com.github.spotbugs")
}

spotbugs {
    excludeFilter = rootProject.file("config/spotbugs/exclusions.xml")
    effort = com.github.spotbugs.snom.Effort.MAX
    reportLevel = com.github.spotbugs.snom.Confidence.LOW
    ignoreFailures = false
    toolVersion = rootProject.libs.versions.spotbugs.version.get()
}

tasks.withType<com.github.spotbugs.snom.SpotBugsTask> {
    reports.create("html") { required = true }
    reports.create("xml") { required = true }
}
