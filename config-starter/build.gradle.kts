val testingModuleName: String by rootProject.extra

dependencies {
    val projectName = project.name

    compileOnly(libs.slf4j)

    api(libs.joor)

    api(libs.jackson.json)
    api(libs.bundles.validation)

    allprojects {

        dependencies {
            testImplementation(rootProject.libs.slf4j)
            testImplementation(rootProject.libs.bundles.log4j)

            testImplementation(rootProject.libs.jackson.json)

            if (!project.name.endsWith(testingModuleName))
                testImplementation(project(":$projectName:$projectName-$testingModuleName"))
        }

    }

    subprojects {

        dependencies {
            compileOnly(rootProject.libs.slf4j)
            api(project(":$projectName"))
        }

    }

}
