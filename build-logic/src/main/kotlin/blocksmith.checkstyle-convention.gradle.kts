/**
 * Plugin for applying uniform Checkstyle conventions across submodules.
 */

plugins {
    checkstyle
}

val buildLogic = gradle.includedBuild("build-logic")
val checkstyleExtension: Configuration by configurations.creating

dependencies {
    checkstyleExtension(
        files(
            buildLogic
                .projectDir
                .resolve("checkstyle-extension/build/libs/checkstyle-extension.jar")
        )
    )
}

checkstyle {
    configFile = rootProject.file("config/checkstyle/checkstyle.xml")
    maxErrors = 0
    maxWarnings = 0
    toolVersion = rootProject.libs.versions.checkstyle.get()
}

tasks.withType<Checkstyle> {
    dependsOn(buildLogic.task(":checkstyle-extension:jar"))
    checkstyleClasspath += checkstyleExtension
    reports {
        xml.required = true
        html.required = true
    }
}
