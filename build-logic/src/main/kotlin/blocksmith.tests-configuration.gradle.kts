@file:Suppress("UnstableApiUsage")
/**
 * Provides other source sets for separated test scopes.
 */
plugins {
    java
    `jvm-test-suite`
}

val sourceSets = listOf("integration", "functional")

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }

        sourceSets.forEach { sourceSet ->

            val testName = "${sourceSet}Test"
            register<JvmTestSuite>(testName) {
                useJUnitJupiter()
                dependencies {
                    implementation(project())
                }
                targets {
                    all {
                        testTask.configure {
                            shouldRunAfter(test)
                        }
                    }
                }
            }

            tasks.named("check") {
                dependsOn(testing.suites.named(testName))
            }

        }
    }
}