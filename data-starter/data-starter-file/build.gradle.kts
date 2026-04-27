dependencies {
    compileOnly(libs.slf4j)

    api(projects.configStarter)

    testImplementation(projects.configStarter.configStarterJson)
}
