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

val minJava = JavaLanguageVersion.of(11)

java {
    toolchain {
        languageVersion.set(currentJava)
    }
}

tasks.compileJava {
    options.release.set(minJava.asInt())
}
