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
    val path = project.path
    val moduleName = project.name

    findProject("$path:$moduleName-$testingModuleName")?.let { testingModule ->

        testingModule.dependencies {
            api(project(":$baseModuleName:$testingModuleName"))
        }

        allprojects {
            if (this.path != testingModule.path)
                afterEvaluate {

                    testing {
                        suites {
                            withType<JvmTestSuite> {
                                dependencies {
                                    implementation(project(testingModule.path))
                                }
                            }
                        }
                    }

                }

        }

    }

}