plugins {
    alias(libs.plugins.shadow)

    id("blocksmith.minecraft-module")
}

val baseModuleName: String by extra

allprojects {
    if (name.endsWith("-$baseModuleName")) return@allprojects

    apply { plugin(rootProject.libs.plugins.shadow.get().pluginId) }

    tasks.jar {
        dependsOn(tasks.shadowJar)

        archiveClassifier = "original"
        archiveBaseName = rootProject.name
    }

    tasks.shadowJar {
        archiveClassifier = ""
        archiveBaseName = rootProject.name

        mergeServiceFiles()
    }

    tasks.processResources {
        val group = "${rootProject.group}"
        val author = group.split(".").last().replaceFirstChar { it.uppercase() }
        val properties = mapOf(
            "group" to group,
            "name" to rootProject.name.replaceFirstChar { it.titlecase() },
            "name_lower" to rootProject.name,
            "description" to "${rootProject.name} plugin for functional testing purposes",
            "version" to "${rootProject.version}",
            "author" to author
        )
        filesMatching("*.yml") { expand(properties) }
        filesMatching("*.json") { expand(properties) }
    }

}

dependencies {
    subprojects.forEach { implementation(it) }
}
