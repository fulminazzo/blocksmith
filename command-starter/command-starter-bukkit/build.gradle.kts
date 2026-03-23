dependencies {
    api(project(":message-starter:message-starter-bukkit"))

    compileOnly(libs.spigot)

    testImplementation(libs.spigot)
}
