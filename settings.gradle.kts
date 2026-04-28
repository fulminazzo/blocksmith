rootProject.name = "blocksmith"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

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

    "config-starter:config-starter-testing",
    "config-starter:config-starter-generator"
)

// example
include("example")

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
