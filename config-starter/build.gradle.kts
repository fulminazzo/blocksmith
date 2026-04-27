dependencies {
    compileOnly(libs.slf4j)
}

allprojects {

    dependencies {
        compileOnly(rootProject.libs.slf4j)

        val testingModule = rootProject.projects.configStarter.configStarterTesting
        if (project.path != testingModule.path) testImplementation(testingModule)
    }

}
