dependencies {
    api(project(":base:validation"))
    api(project(":message-starter"))
    api(project(":scheduler-starter"))

    testImplementation(libs.bundles.log4j)
}
