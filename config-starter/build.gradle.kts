val baseModuleName = "base"
val testingModuleName: String by rootProject.extra

dependencies {
    val projectName = project.name

    compileOnly(libs.slf4j)

    compileOnly(libs.joor)
    compileOnly(libs.bundles.validation)

    testImplementation(libs.joor)
    testImplementation(libs.bundles.validation)

    allprojects {
        dependencies {
            testImplementation(rootProject.libs.slf4j)
            testImplementation(rootProject.libs.bundles.log4j)

            if (!project.name.endsWith(testingModuleName))
                testImplementation(project(":$projectName:$projectName-$testingModuleName"))
        }
    }

    subprojects {
        dependencies {
            compileOnly(rootProject.libs.slf4j)
            if (!project.name.endsWith(baseModuleName))
                api(project(":$projectName:$projectName-$baseModuleName"))
        }
    }

}
