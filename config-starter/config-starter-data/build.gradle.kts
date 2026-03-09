dependencies {
    api(project(":data-starter:data-starter-base"))

    testImplementation(project(":config-starter:config-starter-yaml"))
    testImplementation(project(":data-starter:data-starter-cache"))
    testImplementation(project(":data-starter:data-starter-redis"))
    testImplementation(project(":data-starter:data-starter-sql"))
}