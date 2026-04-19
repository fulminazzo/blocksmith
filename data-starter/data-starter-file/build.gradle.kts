dependencies {
    compileOnly(libs.slf4j)

    api(projects.configStarter)

    testImplementation(libs.slf4j)
    testImplementation(projects.configStarter.configStarterJson)
}
