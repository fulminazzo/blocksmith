dependencies {
    api(project(":message-starter:message-starter-bukkit"))

    compileOnly(libs.spigot)

    testImplementation(libs.spigot)
    testImplementation(libs.mockbukkit16) {
        exclude(group = "io.papermc.paper")
        exclude(group = "net.bytebuddy")
        exclude(group = "org.junit.jupiter")
    }
}
