rootProject.name = "blocksmith"

include("base")

include("example")

include("config-starter")
include("config-starter:config-starter-base")
include("config-starter:config-starter-testing")
include("config-starter:config-starter-json")
include("config-starter:config-starter-properties")
include("config-starter:config-starter-toml")
include("config-starter:config-starter-xml")
include("config-starter:config-starter-yaml")
include("config-starter:config-starter-all")

include("message-starter")

include("message-starter:message-starter-translation")

include("message-starter:message-starter-bukkit")
include("message-starter:message-starter-bungeecord")
include("message-starter:message-starter-velocity")
