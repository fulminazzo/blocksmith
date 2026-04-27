rootProject.name = "blocksmith"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

// base
include(
    "base",

    "base:testing"
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
