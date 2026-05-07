@file:Suppress("UnstableApiUsage")

plugins {
    `java-library`
    groovy

    `jacoco-report-aggregation`
    checkstyle
    codenarc

    alias(libs.plugins.spotbugs)
    alias(libs.plugins.buildconfig)

    id("blocksmith.java-configuration")
    id("blocksmith.tests-configuration")
    id("blocksmith.testing-module-configuration")
}

group = "it.fulminazzo"
version = "0.0.1-SNAPSHOT"

val testingModuleName: String by extra

allprojects {
    apply { plugin("java-library") }
    apply { plugin("groovy") }

    apply { plugin("jacoco") }
    apply { plugin("checkstyle") }
    apply { plugin("codenarc") }

    apply { plugin(rootProject.libs.plugins.spotbugs.get().pluginId) }
    apply { plugin(rootProject.libs.plugins.buildconfig.get().pluginId) }

    apply { plugin("blocksmith.java-configuration") }
    apply { plugin("blocksmith.tests-configuration") }
    apply { plugin("blocksmith.testing-module-configuration") }

    extra["baseModuleName"] = "base"
    extra["testingModuleName"] = "testing"

    val baseModuleName: String by extra

    val projectInfoClassName = "ProjectInfo"

    val mockitoAgent: Configuration by configurations.creating

    dependencies {
        compileOnly(rootProject.libs.bundles.annotations)
        annotationProcessor(rootProject.libs.lombok)

        if (project.path != rootProject.projects.base.path) api(rootProject.projects.base)

        mockitoAgent(rootProject.libs.mockito) { isTransitive = false }
    }

    testing {
        suites {
            withType<JvmTestSuite> {
                useSpock(rootProject.libs.versions.spock.core.get())
                dependencies {
                    rootProject.libs.bundles.annotations.get().forEach { implementation(it) }
                    annotationProcessor(rootProject.libs.lombok.get())

                    implementation(rootProject.projects.base.testing)
                }
                targets {
                    all {
                        testTask.configure {
                            jvmArgs("-javaagent:${mockitoAgent.asPath}")
                            testLogging {
                                exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
                            }
                        }
                    }
                }
            }
        }
    }

    tasks.withType<JacocoReport>().configureEach {
        classDirectories.setFrom(
            files(classDirectories.files.map {
                fileTree(it) {
                    exclude("**/$projectInfoClassName**")
                }
            })
        )
    }

    checkstyle {
        configFile = rootProject.file("config/checkstyle/checkstyle.xml")
        maxErrors = 0
        maxWarnings = 0
        toolVersion = rootProject.libs.versions.checkstyle.get()
    }

    tasks.withType<Checkstyle> {
        reports {
            xml.required = true
            html.required = true
        }
    }

    codenarc {
        configFile = rootProject.file("config/codenarc/codenarc.groovy")
        maxPriority1Violations = 0
        maxPriority2Violations = 0
        maxPriority3Violations = 0
        toolVersion = rootProject.libs.versions.codenarc.get()
    }

    tasks.withType<CodeNarc> {
        reports {
            xml.required = true
            html.required = true
        }
    }

    spotbugs {
        toolVersion = rootProject.libs.versions.spotbugs.version.get()
    }

    configure<com.github.gmazzo.buildconfig.BuildConfigExtension> {
        packageName = "${rootProject.group}.${rootProject.name}"
        className = projectInfoClassName

        var projectName = project.name
        if (project.name.endsWith("-$baseModuleName")) projectName = project.name.removeSuffix("-$baseModuleName")

        buildConfigField("String", "GROUP", "\"${rootProject.group}\"")
        buildConfigField("String", "PROJECT_NAME", "\"${rootProject.name}\"")
        buildConfigField("String", "MODULE_NAME", "\"${projectName}\"")
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

tasks.testCodeCoverageReport {
    dependsOn(tasks.test)
    reports {
        xml.required = true
        csv.required = true
    }
}
