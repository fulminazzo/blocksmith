@file:Suppress("UnstableApiUsage")

/**
 * Testing module configuration plugin.
 *
 * If the project has a testing module, identified by "<module>-<testingModuleName>",
 * each subproject in the module (and itself) will depend on it for testing purposes.
 */

plugins {
    `java-library`
    `jvm-test-suite`
}

afterEvaluate {
    val baseModuleName: String by extra
    val testingModuleName: String by extra

    dependencies {
        val path = project.path

        findProject("$path:$path-$testingModuleName")?.let { testingModule ->

            testingModule.dependencies {
                api(project(":$baseModuleName:$testingModuleName"))
            }

            allprojects {

                testing {
                    suites {
                        withType<JvmTestSuite> {
                            dependencies {
                                if (project.path != testingModule.path)
                                    implementation(testingModule)
                            }
                        }
                    }
                }

            }

        }

    }

}