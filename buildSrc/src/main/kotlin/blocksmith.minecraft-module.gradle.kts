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

val libs = the<VersionCatalogsExtension>().named("libs")
val path = project.path

val projects = mutableListOf("bukkit", "bungeecord", "velocity")
if (extension.enableFolia.getOrElse(false)) projects.add("folia")

projects.forEach {
    project("$path$path-$it") {

        repositories {
            mavenCentral()
            maven {
                name = "spigotmc-repo"
                url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
            }
            maven {
                name = "papermc"
                url = uri("https://repo.papermc.io/repository/maven-public/")
            }
        }

        dependencies {
            val dependency = libs.findLibrary(it).get()
            compileOnly(dependency)
            testImplementation(dependency)
        }

    }
}

extensions.configure<CompositeModuleExtension> {
    importToParent = false
}
