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
val currentCompiler = javaToolchains.compilerFor { languageVersion = currentJava }
val currentLauncher = javaToolchains.launcherFor { languageVersion = currentJava }

val minJava = JavaLanguageVersion.of(11)

java {
    toolchain {
        languageVersion.set(minJava)
    }
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
    javaLauncher = currentLauncher
}

