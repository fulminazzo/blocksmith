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

    "config-starter:config-starter-data",

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

    "data-starter:data-starter-file",
    "data-starter:data-starter-memory",
    "data-starter:data-starter-mongo",
    "data-starter:data-starter-redis",
    "data-starter:data-starter-sql",

    "data-starter:data-starter-cache",

    "data-starter:data-starter-testing"
)

// message-broker-starter
include(
    "message-broker-starter",

    "message-broker-starter:message-broker-starter-base",

    "message-broker-starter:message-broker-starter-memory",
    "message-broker-starter:message-broker-starter-redis",

    "message-broker-starter:message-broker-starter-testing"
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
