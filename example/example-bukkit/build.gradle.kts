plugins {
    alias(libs.plugins.shadow)
}

dependencies {
    implementation(libs.slf4j.jdk)

    implementation(project(":message-starter:message-starter-bukkit"))
    implementation(project(":message-starter:message-starter-translation"))

    compileOnly(libs.spigot)

    testImplementation(libs.spigot)
}

tasks.jar {
    dependsOn(tasks.shadowJar)
    archiveBaseName = getFileBaseName()
    archiveClassifier = "original"
}

tasks.shadowJar {
    archiveBaseName = getFileBaseName()
    archiveClassifier = ""
}

tasks.processResources {
    val properties = mapOf(
        "version" to rootProject.version,
        "group" to rootProject.group,
        "name" to getProjectName(),
        "name_lower" to rootProject.name
    )
    inputs.properties(properties)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(properties)
    }
}

fun getFileBaseName(): String = "${getProjectName()}-${rootProject.version}"

fun getProjectName(): String = rootProject.name.replaceFirstChar { it.uppercase() }
