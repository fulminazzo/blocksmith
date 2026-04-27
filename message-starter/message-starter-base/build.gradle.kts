allprojects {
    dependencies {
        api(rootProject.libs.bundles.adventure.text)
    }
}

dependencies {
    api(libs.slf4j)
    api(projects.configStarterBase)

    testImplementation(projects.configStarter.configStarterYaml)
}
