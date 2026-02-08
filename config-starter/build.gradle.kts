dependencies {
    val projectName = project.name

    compileOnly(libs.slf4j)

    api(libs.jackson.json)
    api(libs.bundles.validation)

    subprojects {

        dependencies {
            compileOnly(rootProject.libs.slf4j)
            api(project(":$projectName"))

            val testingModuleName = "$projectName-testing"
            if (project.name != testingModuleName)
                testImplementation(project(":$projectName:$testingModuleName"))
        }

    }

    testImplementation(libs.slf4j)
    testImplementation(libs.bundles.log4j)

    testImplementation(libs.jackson.json)
    testImplementation(project(":$projectName:$projectName-testing"))

}
