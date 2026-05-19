plugins { id("blocksmith.composite-module") }

dependencies {
    integrationTestImplementation(projects.configStarter.configStarterJson)
    integrationTestImplementation(libs.h2)
    integrationTestImplementation(libs.postgresql)
    integrationTestImplementation(libs.test.containers.postgresql)
    integrationTestImplementation(libs.test.containers.mongodb)
}

compositeModule {
    ignoredSubmodules = setOf(project.projects.dataStarter.dataStarterMapper.name)
}
