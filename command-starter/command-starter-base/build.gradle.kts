dependencies {
    api(projects.validationStarter)
    api(projects.messageStarter)
    api(projects.schedulerStarter)

    testImplementation(libs.bundles.log4j)
}
