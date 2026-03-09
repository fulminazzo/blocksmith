plugins {
    alias(libs.plugins.shadow)
}

dependencies {
    compileOnly(libs.spigot)

    testImplementation(libs.spigot)
}

tasks.jar {
    dependsOn(tasks.shadowJar)
    archiveClassifier = "original"
}

tasks.shadowJar {
    archiveClassifier = ""
}

tasks.processResources {
    val properties = mapOf(
        "version" to version,
        "group" to group,
        "name" to rootProject.name,
        "name_lower" to rootProject.name.lowercase()
    )
    inputs.properties(properties)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(properties)
    }
}
