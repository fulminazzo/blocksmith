dependencies {
    compileOnly(libs.slf4j)
}

allprojects {

    dependencies {
        val testingModule = rootProject.projects.configStarter.configStarterTesting
        if (project.path != testingModule.path) testImplementation(testingModule)
    }

}

subprojects {

    dependencies {
        compileOnly(rootProject.libs.slf4j)

        val coreModule = rootProject.projects.configStarter.configStarterCore
        if (project.path != coreModule.path) api(coreModule)
    }

}
