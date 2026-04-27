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

    "message-starter:message-starter-base",

    "message-starter:message-starter-translation",

    "message-starter:message-starter-bukkit",
    "message-starter:message-starter-bungeecord",
    "message-starter:message-starter-velocity"
)

// scheduler-starter
include(
    "scheduler-starter",

    "scheduler-starter:scheduler-starter-base",

    "scheduler-starter:scheduler-starter-bukkit",
    "scheduler-starter:scheduler-starter-folia",
    "scheduler-starter:scheduler-starter-bungeecord",
    "scheduler-starter:scheduler-starter-velocity"
)

// command-starter
include(
    "command-starter",
    "command-starter:command-starter-base",
    "command-starter:command-starter-brigadier",

    "command-starter:command-starter-bukkit",
    "command-starter:command-starter-bungeecord",
    "command-starter:command-starter-velocity"
)

// example
include(
    "example",

    "example:example-bukkit"
)
