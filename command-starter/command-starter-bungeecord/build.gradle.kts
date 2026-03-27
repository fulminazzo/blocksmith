dependencies {
    api(project(":message-starter:message-starter-bungeecord"))
    api(project(":scheduler-starter:scheduler-starter-bungeecord"))

    compileOnly(libs.bungeecord)

    testImplementation(libs.bungeecord)
}
