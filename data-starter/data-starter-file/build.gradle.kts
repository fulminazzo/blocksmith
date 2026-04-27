dependencies {
    compileOnly(libs.slf4j)

    api(projects.configStarter.configStarterBase)

    testImplementation(projects.configStarter.configStarterJson)
}
