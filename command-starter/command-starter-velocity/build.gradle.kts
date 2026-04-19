dependencies {
    api(projects.messageStarter.messageStarterVelocity)
    api(projects.schedulerStarter.schedulerStarterVelocity)

    api(projects.commandStarter.commandStarterBrigadier)

    compileOnly(libs.velocity)

    testImplementation(libs.velocity)
}
