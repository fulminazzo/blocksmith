plugins {
    id("java-library")
    id("groovy")
    id("jacoco-report-aggregation")
}

group = "it.fulminazzo"
version = "0.0.1-SNAPSHOT"

allprojects {
    apply { plugin("java-library") }
    apply { plugin("groovy") }
    apply { plugin("jacoco-report-aggregation") }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(11))
        }
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        compileOnly(rootProject.libs.bundles.annotations)
        annotationProcessor(rootProject.libs.lombok)

        testImplementation(rootProject.libs.bundles.annotations)
        testImplementation(rootProject.libs.bundles.test.framework)
    }

    tasks.test {
        useJUnitPlatform()
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
