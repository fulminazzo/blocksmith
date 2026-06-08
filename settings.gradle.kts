@file:Suppress("UnstableApiUsage")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
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

}

pluginManagement {
    includeBuild("build-logic")
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "blocksmith"

// base
include(
    "base",

    "base:testing"
)

// functional testing
include(
    "functional-test",

    "functional-test:functional-test-base",
    "functional-test:functional-test-bukkit",
    "functional-test:functional-test-bungeecord",
    "functional-test:functional-test-velocity"
)
