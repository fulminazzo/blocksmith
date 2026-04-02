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

    val currentJava = JavaLanguageVersion.of(Runtime.version().feature())

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
        testRuntimeOnly(rootProject.libs.junit.platform)
        testImplementation(rootProject.libs.bundles.test.framework)
        testAnnotationProcessor(rootProject.libs.lombok)
    }

    tasks.withType<GroovyCompile> {
        javaLauncher = javaToolchains.launcherFor {
            languageVersion = currentJava
        }
    }

    tasks.compileTestJava {
        javaCompiler = javaToolchains.compilerFor {
            languageVersion = currentJava
        }
    }

    tasks.test {
        useJUnitPlatform()
        javaLauncher = javaToolchains.launcherFor {
            languageVersion = currentJava
        }
    }

}

/**
 * TESTING MODULES CONFIGURATION
 */
subprojects {
    val testingModuleName: String by rootProject.extra

    if (project.name.endsWith(testingModuleName)) {
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
