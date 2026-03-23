dependencies {
    api(project(":message-starter:message-starter-bungeecord"))

    compileOnly(libs.bungeecord)

    testImplementation(libs.bungeecord)
}
