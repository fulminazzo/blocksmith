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

// message-starter
include(
    "message-starter",
    "message-starter:message-starter-translation",

    "message-starter:message-starter-bukkit",
    "message-starter:message-starter-bungeecord",
    "message-starter:message-starter-velocity"
)

// example
include(
    "example",

    "example:example-bukkit"
)
