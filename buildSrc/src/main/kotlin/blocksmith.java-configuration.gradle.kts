/**
 * Java configuration plugin.
 *
 * Tests will run under the provided Java version (to provide support for newer features).
 * However, the main project will remain on Java 11 for legacy versions compatibility.
 */

plugins {
    java
    groovy
}

val currentJava = JavaLanguageVersion.of(Runtime.version().feature())

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
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
    javaLauncher = javaToolchains.launcherFor {
        languageVersion = currentJava
    }
}
