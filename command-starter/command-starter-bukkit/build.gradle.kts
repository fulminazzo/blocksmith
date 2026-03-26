dependencies {
    api(project(":message-starter:message-starter-bukkit"))
    api(project(":command-starter:command-starter-brigadier"))

    compileOnly(libs.spigot)
    compileOnly(libs.brigadier)

    testImplementation(libs.spigot)
    testImplementation(libs.brigadier)
    testImplementation(libs.mockbukkit16) {
        exclude(group = "io.papermc.paper")
        exclude(group = "net.bytebuddy")
        exclude(group = "org.junit.jupiter")
    }
}
