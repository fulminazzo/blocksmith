plugins {
    id("java-library")
    id("groovy")
    id("jacoco-report-aggregation")
}

group = "it.fulminazzo"
version = "0.0.1-SNAPSHOT"

extra["testingModuleName"] = "testing"

allprojects {
    apply { plugin("java-library") }
    apply { plugin("groovy") }
    apply { plugin("jacoco-report-aggregation") }

    repositories {
        mavenCentral()
    }

    dependencies {
        compileOnly(rootProject.libs.bundles.annotations)
        annotationProcessor(rootProject.libs.lombok)

        testImplementation(rootProject.libs.bundles.annotations)
        testAnnotationProcessor(rootProject.libs.lombok)
        testImplementation(rootProject.libs.bundles.test.framework)
    }

    tasks.test {
        useJUnitPlatform()
    }

}

/**
 * TESTING MODULES CONFIGURATION
 */
subprojects {
    val testingModuleName: String by rootProject.extra

    if (project.name.endsWith("-$testingModuleName")) {
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
