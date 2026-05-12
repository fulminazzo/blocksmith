@file:Suppress("UnstableApiUsage")

plugins {
    `java-library`
    groovy

    id("blocksmith.java-configuration")
    id("blocksmith.sonarqube-configuration")
}

group = "it.fulminazzo"
version = "0.0.1-SNAPSHOT"

val testingModuleName: String by extra
val projectInfoClassName: String by extra

allprojects {
    extra["baseModuleName"] = "base"
    extra["testingModuleName"] = "testing"
    extra["projectInfoClassName"] = "ProjectInfo"

    apply { plugin("java-library") }
    apply { plugin("groovy") }

    apply { plugin("blocksmith.java-configuration") }
    apply { plugin("blocksmith.tests-configuration") }
    apply { plugin("blocksmith.testing-module-configuration") }

    apply { plugin("blocksmith.buildconfig-configuration") }
    apply { plugin("blocksmith.checkstyle-configuration") }
    apply { plugin("blocksmith.codenarc-configuration") }
    apply { plugin("blocksmith.jacoco-configuration") }
    apply { plugin("blocksmith.spotbugs-configuration") }

    dependencies {
        compileOnly(rootProject.libs.bundles.annotations)
        compileOnly(libs.spotbugs.annotations)
        annotationProcessor(rootProject.libs.lombok)

        if (project.path != rootProject.projects.base.path) api(rootProject.projects.base)
    }

    testing {
        suites {
            withType<JvmTestSuite> {
                useSpock(rootProject.libs.versions.spock.core.get())
                dependencies {
                    rootProject.libs.bundles.annotations.get().forEach { implementation(it) }
                    annotationProcessor(rootProject.libs.lombok.get())
                    compileOnly(libs.spotbugs.annotations)

                    implementation(libs.mockito)

                    implementation(rootProject.projects.base.testing)
                }
                targets {
                    all {
                        testTask.configure {
                            testLogging {
                                exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
                            }
                        }
                    }
                }
            }
        }
    }

}

subprojects {

    /**
     * TESTING MODULES CONFIGURATION
     */
    if (project.name.endsWith(testingModuleName)) {
        apply { plugin("groovy") }

        dependencies {
            implementation(rootProject.libs.bundles.test.framework)
        }
    }

}

dependencies {
    subprojects
        .filter { !it.name.endsWith("-$testingModuleName") }
        .forEach { implementation(it) }
}

tasks.register<JacocoReport>("jacocoAggregatedReport") {
    description = "Generates a JaCoCo report aggregating all subprojects reports."
    group = "Verification"

    val subprojects = rootProject.subprojects.filter { !it.name.endsWith(testingModuleName) }

    dependsOn(subprojects.flatMap { it.tasks.withType<Test>() })

    executionData.setFrom(
        subprojects.map { fileTree(it.layout.buildDirectory).include("jacoco/*.exec") }
    )

    sourceDirectories.setFrom(
        subprojects.flatMap {
            it.extensions.findByType<JavaPluginExtension>()
                ?.sourceSets?.getByName("main")?.allSource?.srcDirs
                ?: emptySet()
        }
    )

    classDirectories.setFrom(
        subprojects.flatMap {
            it.extensions.findByType<JavaPluginExtension>()
                ?.sourceSets?.getByName("main")?.output?.classesDirs
                ?: emptyList()
        }.map { fileTree(it) { exclude("**/$projectInfoClassName**") } }
    )

    reports {
        xml.required = true
        html.required = true
        csv.required = true
    }
}

tasks.check {
    dependsOn(tasks.named("jacocoAggregatedReport"))
}
