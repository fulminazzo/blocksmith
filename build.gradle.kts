plugins {
    id("java-library")
    id("groovy")
    id("jacoco-report-aggregation")
}

group = "it.fulminazzo"
version = "0.0.1-SNAPSHOT"

extra["baseModuleName"] = "base"
extra["testingModuleName"] = "testing"

allprojects {
    apply { plugin("java-library") }
    apply { plugin("groovy") }
    apply { plugin("jacoco-report-aggregation") }

    val baseModuleName: String by rootProject.extra
    val testingModuleName: String by rootProject.extra

    val currentJava = JavaLanguageVersion.of(Runtime.version().feature())
    val currentCompiler = javaToolchains.compilerFor { languageVersion = currentJava }
    val currentLauncher = javaToolchains.launcherFor { languageVersion = currentJava }

    val mockitoAgent: Configuration by configurations.creating

    val minJava = JavaLanguageVersion.of(11)

    java {
        toolchain {
            languageVersion.set(minJava)
        }
    }

    repositories {
        mavenCentral()
        maven {
            name = "spigotmc-repo"
            url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        }
        maven {
            name = "papermc"
            url = uri("https://repo.papermc.io/repository/maven-public/")
        }
    }

    dependencies {
        compileOnly(rootProject.libs.bundles.annotations)
        annotationProcessor(rootProject.libs.lombok)

        if (project.name != baseModuleName) api(project(":$baseModuleName"))

        testImplementation(rootProject.libs.bundles.annotations)
        testRuntimeOnly(rootProject.libs.junit.platform)
        testAnnotationProcessor(rootProject.libs.lombok)
        testImplementation(rootProject.libs.bundles.test.framework)

        testImplementation(project(":$baseModuleName:$testingModuleName"))

        mockitoAgent(rootProject.libs.mockito) { isTransitive = false }
    }

    tasks.compileJava {
        javaCompiler = currentCompiler
        options.release.set(minJava.asInt())
    }

    tasks.withType<GroovyCompile> {
        javaLauncher = currentLauncher
    }

    tasks.compileTestJava {
        javaCompiler = currentCompiler
    }

    tasks.test {
        useJUnitPlatform()
        jvmArgs("-javaagent:${mockitoAgent.asPath}")
        javaLauncher = currentLauncher
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
