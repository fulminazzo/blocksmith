val testingModuleName: String by rootProject.extra

dependencies {
    val projectName = project.name

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

}
