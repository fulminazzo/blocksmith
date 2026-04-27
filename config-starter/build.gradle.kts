dependencies {
    compileOnly(libs.slf4j)
}

allprojects {

    dependencies {
        compileOnly(rootProject.libs.slf4j)

        val baseModule = rootProject.projects.configStarter.configStarterBase
        if (project.path != baseModule.path) api(baseModule)

        val testingModule = rootProject.projects.configStarter.configStarterTesting
        if (project.path != testingModule.path) testImplementation(testingModule)
    }

}
