plugins {
    `java-library`
    groovy
    `jacoco-report-aggregation`

    alias(libs.plugins.buildconfig)
}

group = "it.fulminazzo"
version = "0.0.1-SNAPSHOT"

val testingModuleName: String by extra

allprojects {
    apply { plugin("java-library") }
    apply { plugin("groovy") }
    apply { plugin("jacoco") }
    apply { plugin(rootProject.libs.plugins.buildconfig.get().pluginId) }

    apply { plugin("blocksmith.java-configuration")}
    apply { plugin("blocksmith.testing-module-configuration")}

    extra["baseModuleName"] = "base"
    extra["testingModuleName"] = "testing"

    val baseModuleName: String by extra

    val projectInfoClassName = "ProjectInfo"

    val mockitoAgent: Configuration by configurations.creating

    repositories {
        mavenCentral()
    }

    dependencies {
        compileOnly(rootProject.libs.bundles.annotations)
        annotationProcessor(rootProject.libs.lombok)

        if (project.path != rootProject.projects.base.path) api(rootProject.projects.base)

        testImplementation(rootProject.libs.bundles.annotations)
        testRuntimeOnly(rootProject.libs.junit.platform)
        testAnnotationProcessor(rootProject.libs.lombok)
        testImplementation(rootProject.libs.bundles.test.framework)

        testImplementation(rootProject.projects.base.testing)

        mockitoAgent(rootProject.libs.mockito) { isTransitive = false }
    }

    tasks.test {
        useJUnitPlatform()
        jvmArgs("-javaagent:${mockitoAgent.asPath}")
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

    tasks.withType<JacocoReport>().configureEach {
        classDirectories.setFrom(
            files(classDirectories.files.map {
                fileTree(it) {
                    exclude("**/$projectInfoClassName**", "**/data/jooq/**")
                }
            })
        )
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
