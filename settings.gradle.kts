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
    "data-starter:data-starter-testing",

    "data-starter:data-starter-mapper",
    "data-starter:data-starter-mapper:data-starter-mapper-json",

    "data-starter:data-starter-file",
    "data-starter:data-starter-memory",
    "data-starter:data-starter-mongo",
    "data-starter:data-starter-redis",
    "data-starter:data-starter-sql",

    "data-starter:data-starter-cache",

    "data-starter:data-starter-all"
)

// example
include("example")
