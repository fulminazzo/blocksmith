dependencies {
    api(projects.base.validation)
    api(projects.messageStarter)
    api(projects.schedulerStarter)

    testImplementation(libs.bundles.log4j)
}
