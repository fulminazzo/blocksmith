dependencies {
    api(projects.messageStarter.messageStarterBukkit)
    api(projects.schedulerStarter.schedulerStarterBukkit)
    api(projects.schedulerStarter.schedulerStarterFolia)

    api(projects.commandStarter.commandStarterBrigadier)

    compileOnly(libs.brigadier)

    testImplementation(libs.brigadier)
    testImplementation(libs.mockbukkit16) {
        exclude(group = "io.papermc.paper")
        exclude(group = "net.bytebuddy")
        exclude(group = "org.junit.jupiter")
    }
}
