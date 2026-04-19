allprojects {
    dependencies {
        api(rootProject.libs.bundles.adventure.text)
    }
}

subprojects {
    dependencies {
        project.parent?.let { api(it) }
    }
}

dependencies {
    api(libs.slf4j)
    api(projects.configStarter)

    testImplementation(projects.configStarter.configStarterYaml)
}
