dependencies {
    api(projects.validationStarter)
    api(projects.messageStarter.messageStarterBase)
    api(projects.schedulerStarter.schedulerStarterBase)

    testImplementation(libs.bundles.log4j)
}
