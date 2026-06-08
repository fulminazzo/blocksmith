import CompositeModuleExtension
import org.gradle.kotlin.dsl.create

/**
 * Convention plugin identifying a Minecraft module.
 *
 * A Minecraft module requires a "<module>-<baseModuleName>" subproject to be passed.
 * Each subproject in the module will depend on the base subproject.
 * Then, at least three modules must be available:
 * - "<module>-bukkit"
 * - "<module>-bungeecord"
 * - "<module>-velocity"
 * ("<module>-folia" is optional and can be toggled).
 */

plugins {
    `java-library`
    id("blocksmith.composite-module")
}

interface MinecraftModuleExtension {
    val enableFolia: Property<Boolean>
}

val extension = extensions.create<MinecraftModuleExtension>("minecraftModule")

extensions.configure<CompositeModuleExtension> {
    importToParent = false
}

afterEvaluate {

    val libraries = mutableMapOf(
        "bukkit" to libs.bukkit,
        "bungeecord" to libs.bungeecord,
        "velocity" to libs.velocity
    )
    if (extension.enableFolia.getOrElse(false)) libraries["folia"] = libs.folia

    libraries.forEach { (name, library) ->
        project("${project.path}:${project.name}-$name") {

            dependencies {
                compileOnly(library)
                testImplementation(library)
                if (name.equals(libs.velocity.get().name, ignoreCase = true)) {
                    annotationProcessor(libs)
                    testAnnotationProcessor(libs)
                }
            }

        }
    }

}
