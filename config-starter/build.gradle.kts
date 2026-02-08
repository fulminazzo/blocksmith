dependencies {
    val projectName = project.name

    compileOnly(libs.slf4j)

    api(libs.jackson.json)
    api(libs.bundles.validation)

    allprojects {

        dependencies {
            testImplementation(rootProject.libs.slf4j)
            testImplementation(rootProject.libs.bundles.log4j)

            testImplementation(rootProject.libs.jackson.json)

            val testingModuleName = "$projectName-testing"
            if (project.name != testingModuleName)
                testImplementation(project(":$projectName:$projectName-testing"))
        }

    }

    subprojects {

        dependencies {
            compileOnly(rootProject.libs.slf4j)
            api(project(":$projectName"))
        }

    }

}
