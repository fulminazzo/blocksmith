/**
 * Testing module configuration plugin.
 *
 * If the project has a testing module, identified by "<module>-<testingModuleName>",
 * each subproject in the module (and itself) will depend on it for testing purposes.
 */

plugins {
    `java-library`
}

afterEvaluate {
    val baseModuleName: String by extra
    val testingModuleName: String by extra

    dependencies {
        val path = project.path

        findProject("$path:$path-$testingModuleName")?.let { testingModule ->

            dependencies {
                api(findProject(":$baseModuleName:$testingModuleName"))
            }

            allprojects {

                dependencies {
                    if (project.path != testingModule.path)
                        testImplementation(testingModule)
                }

            }

        }

    }

}