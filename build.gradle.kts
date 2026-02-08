plugins {
    id("java-library")
    id("groovy")
    id("jacoco-report-aggregation")

    alias(libs.plugins.buildconfig)
}

group = "it.fulminazzo"
version = "0.0.1-SNAPSHOT"

allprojects {
    apply { plugin("java-library") }
    apply { plugin("groovy") }
    apply { plugin("jacoco-report-aggregation") }
    apply { plugin(rootProject.libs.plugins.buildconfig.get().pluginId) }

    repositories {
        mavenCentral()
    }

    dependencies {
        compileOnly(rootProject.libs.bundles.annotations)
        annotationProcessor(rootProject.libs.lombok)

        val baseProjectName = "base"
        if (project.name != baseProjectName) api(project(":$baseProjectName"))

        testImplementation(rootProject.libs.bundles.annotations)
        testAnnotationProcessor(rootProject.libs.lombok)
        testImplementation(rootProject.libs.bundles.test.framework)
    }

    tasks.test {
        useJUnitPlatform()
    }

    configure<com.github.gmazzo.buildconfig.BuildConfigExtension> {
        packageName = "${rootProject.group}.${rootProject.name}"
        className = "ProjectInfo"

        buildConfigField("String", "GROUP", "\"${rootProject.group}\"")
        buildConfigField("String", "PROJECT_NAME", "\"${rootProject.name}\"")
        buildConfigField("String", "MODULE_NAME", "\"${project.name}\"")
    }

}

/**
 * TESTING MODULES CONFIGURATION
 */
subprojects {
    if (project.name.endsWith("-testing")) {
        apply { plugin("groovy") }

        dependencies {
            implementation(rootProject.libs.bundles.test.framework)
        }
    }

}

dependencies {
    subprojects.forEach { implementation(project(it.path)) }
}

tasks.testCodeCoverageReport {
    dependsOn(tasks.test)
    reports {
        xml.required = true
        csv.required = true
    }
}
