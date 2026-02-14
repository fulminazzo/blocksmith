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


include("data-starter")
include("data-starter:data-starter-base")
include("data-starter:data-starter-testing")

include("data-starter:data-starter-mapper")
include("data-starter:data-starter-mapper:data-starter-mapper-json")

include("data-starter:data-starter-file")
include("data-starter:data-starter-mongo")
include("data-starter:data-starter-redis")
include("data-starter:data-starter-sql")

include("base:validation")