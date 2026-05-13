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

// validation-starter
include("validation-starter")

// config-starter
include(
    "config-starter",

    "config-starter:config-starter-base",

    "config-starter:config-starter-json",
    "config-starter:config-starter-properties",
    "config-starter:config-starter-toml",
    "config-starter:config-starter-xml",
    "config-starter:config-starter-yaml",

//    "config-starter:config-starter-data", //TODO: re-enable

    "config-starter:config-starter-testing",
    "config-starter:config-starter-generator"
)

// data-starter
include(
    "data-starter",

    "data-starter:data-starter-base",

    "data-starter:data-starter-mapper",
    "data-starter:data-starter-mapper:data-starter-mapper-base",
    "data-starter:data-starter-mapper:data-starter-mapper-json",

//    "data-starter:data-starter-file", //TODO: re-enable
//    "data-starter:data-starter-memory", //TODO: re-enable
//    "data-starter:data-starter-mongo", //TODO: re-enable
//    "data-starter:data-starter-redis", //TODO: re-enable
//    "data-starter:data-starter-sql", //TODO: re-enable

//    "data-starter:data-starter-cache", //TODO: re-enable

    "data-starter:data-starter-testing"
)

@Suppress("UnstableApiUsage")
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
