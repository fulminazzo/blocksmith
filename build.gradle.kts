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
    apply { plugin("jacoco") }
    apply { plugin(rootProject.libs.plugins.buildconfig.get().pluginId) }

    extra["baseModuleName"] = "base"
    extra["testingModuleName"] = "testing"
    extra["excludedSubmodules"] = mutableSetOf<String>()

    val baseModuleName: String by rootProject.extra

    val projectInfoClassName = "ProjectInfo"

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

        if (project.path != rootProject.projects.base.path) api(rootProject.projects.base)

        testImplementation(rootProject.libs.bundles.annotations)
        testRuntimeOnly(rootProject.libs.junit.platform)
        testAnnotationProcessor(rootProject.libs.lombok)
        testImplementation(rootProject.libs.bundles.test.framework)

        testImplementation(rootProject.projects.base.testing)

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

    configure<com.github.gmazzo.buildconfig.BuildConfigExtension> {
        packageName = "${rootProject.group}.${rootProject.name}"
        className = projectInfoClassName

        buildConfigField("String", "GROUP", "\"${rootProject.group}\"")
        buildConfigField("String", "PROJECT_NAME", "\"${rootProject.name}\"")
        buildConfigField("String", "MODULE_NAME", "\"${project.name.replace("-$baseModuleName", "")}\"")
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
    val baseModuleName: String by extra
    val testingModuleName: String by extra

    val excludedSubmodules: MutableSet<String> by extra

    afterEvaluate {

        /*
         * A module is treated as a "composite module" if it contains a subproject
         * matching the pattern "<module>:<module>-<baseName>" (e.g. "command:command-base").
         *
         * In that case, the dependency graph is structured as follows:
         * - every other subproject in the module depends on the base subproject;
         * - the parent project depends on all subprojects (except testing).
         */
        dependencies {
            val path = project.path

            // base
            findProject("$path$path-$baseModuleName")?.let { baseModule ->
                subprojects
                    .filter { !it.name.endsWith(testingModuleName) }
                    .filter { it.name !in excludedSubmodules }
                    .forEach { api(it) }

                subprojects {
                    dependencies {
                        if (project.path != baseModule.path) api(baseModule)
                    }
                }

            }

            // testing
            findProject("$path:$path-$testingModuleName")?.let { testingModule ->

                allprojects {
                    dependencies {
                        if (project.path != testingModule.path)
                            testImplementation(testingModule)
                    }
                }

            }

        }

    }

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
    val testingModuleName: String by rootProject.extra

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
