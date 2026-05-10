/**
 * Plugin for applying the Blocksmith Codenarc configuration across submodules.
 */

plugins {
    codenarc
}

codenarc {
    configFile = rootProject.file("config/codenarc/codenarc.groovy")
    maxPriority1Violations = 0
    maxPriority2Violations = 0
    maxPriority3Violations = 0
    toolVersion = rootProject.libs.versions.codenarc.get()
}

tasks.withType<CodeNarc> {
    reports {
        xml.required = true
        html.required = true
    }
}