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

    val currentJava = JavaLanguageVersion.of(Runtime.version().feature())
    val mockitoAgent: Configuration by configurations.creating

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

        mockitoAgent(rootProject.libs.mockito) { isTransitive = false }
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
        jvmArgs("-javaagent:${mockitoAgent.asPath}")
        javaLauncher = javaToolchains.launcherFor {
            languageVersion = currentJava
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
