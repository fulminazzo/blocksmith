allprojects {

    dependencies {
        val testingModule = rootProject.projects.dataStarter.dataStarterTesting
        if (project.path != testingModule.path) testImplementation(testingModule)
    }

}

subprojects {

    dependencies {
        val baseModule = rootProject.projects.dataStarter.dataStarterBase
        if (project.path != baseModule.path) api(baseModule)
    }

}
