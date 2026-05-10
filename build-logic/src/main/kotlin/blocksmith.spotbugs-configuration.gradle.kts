/**
 * Plugin for applying the Blocksmith Spotbugs configuration across submodules.
 */

plugins {
    id("com.github.spotbugs")
}

spotbugs {
    excludeFilter = rootProject.file("config/spotbugs/exclusions.xml")
    toolVersion = rootProject.libs.versions.spotbugs.version.get()
}

tasks.withType<com.github.spotbugs.snom.SpotBugsTask> {
    reports {
        create("xml") {
            required.set(true)
        }
        create("html") {
            required.set(true)
        }
    }
}
