dependencies {
    api(projects.messageStarter.messageStarterBungeecord)
    api(projects.schedulerStarter.schedulerStarterBungeecord)

    compileOnly(libs.bungeecord)

    testImplementation(libs.bungeecord)
}
