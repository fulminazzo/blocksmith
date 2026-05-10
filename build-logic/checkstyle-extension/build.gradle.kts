@file:Suppress("UnstableApiUsage")

plugins {
    groovy
    java
}

dependencies {
    compileOnly(rootProject.libs.bundles.annotations)
    annotationProcessor(rootProject.libs.lombok)

    implementation(libs.checkstyle)
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }

        register<JvmTestSuite>("functionalTest") {
            useJUnitJupiter()
            dependencies {
                implementation(project())
                implementation(libs.checkstyle)
            }
            targets {
                all {
                    testTask.configure {
                        shouldRunAfter(test)
                    }
                }
            }
        }

        withType<JvmTestSuite> {
            useSpock(rootProject.libs.versions.spock.core.get())
            dependencies {
                rootProject.libs.bundles.annotations.get().forEach { implementation(it) }
                annotationProcessor(rootProject.libs.lombok.get())

                implementation(libs.mockito)
            }
        }

        tasks.named("check") {
            dependsOn(testing.suites.named("functionalTest"))
        }

    }
}
