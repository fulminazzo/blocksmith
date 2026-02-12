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

        val baseProjectName = "base"
        if (!project.name.endsWith(baseProjectName))
            api(project(":$projectName:$projectName-$baseProjectName"))

    }
}
