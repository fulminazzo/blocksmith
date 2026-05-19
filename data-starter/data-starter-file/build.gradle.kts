dependencies {
    compileOnly(libs.slf4j)

    api(projects.configStarter.configStarterBase)

    integrationTestImplementation(projects.configStarter.configStarterJson)
}
