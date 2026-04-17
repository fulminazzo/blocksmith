rootProject.name = "blocksmith"

include("base")
include("base:validation")

include("base:testing")


include("example")
include("example:example-bukkit")


include("config-starter")
include("config-starter:config-starter-core")

include("config-starter:config-starter-json")
include("config-starter:config-starter-properties")
include("config-starter:config-starter-toml")
include("config-starter:config-starter-xml")
include("config-starter:config-starter-yaml")

include("config-starter:config-starter-all")

include("config-starter:config-starter-testing")
include("config-starter:config-starter-generator")


include("message-starter")
include("message-starter:message-starter-translation")

include("message-starter:message-starter-bukkit")
include("message-starter:message-starter-bungeecord")
include("message-starter:message-starter-velocity")

include("scheduler-starter")

include("scheduler-starter:scheduler-starter-bukkit")
include("scheduler-starter:scheduler-starter-folia")
include("scheduler-starter:scheduler-starter-bungeecord")
include("scheduler-starter:scheduler-starter-velocity")

include("command-starter")
include("command-starter:command-starter-base")
//TODO: re-include
//include("command-starter:command-starter-brigadier")

//include("command-starter:command-starter-bukkit")
//include("command-starter:command-starter-bungeecord")
//include("command-starter:command-starter-velocity")

