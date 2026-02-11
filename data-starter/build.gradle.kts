val projectName: String = project.name
val testingModuleName: String by rootProject.extra

allprojects {
    dependencies {
        if (!project.name.endsWith(testingModuleName))
            testImplementation(project(":$projectName:$projectName-$testingModuleName"))
    }
}

subprojects {
    dependencies {
        api(project(":$projectName"))
    }
}
