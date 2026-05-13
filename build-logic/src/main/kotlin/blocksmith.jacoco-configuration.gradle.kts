/**
 * Plugin for applying the Blocksmith JaCoCo configuration across submodules.
 */

plugins {
    jacoco
}

val projectInfoClassName: String by extra

tasks.withType<JacocoReport>().configureEach {
    dependsOn(tasks.withType<Test>())

    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                exclude("**/$projectInfoClassName**")
            }
        })
    )

    executionData.setFrom(
        fileTree(layout.buildDirectory).include("jacoco/*.exec")
    )

    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.withType<JacocoCoverageVerification>().configureEach {
    dependsOn(tasks.withType<Test>())

    executionData.setFrom(
        fileTree(layout.buildDirectory).include("jacoco/*.exec")
    )

    violationRules {
        rule {
            excludes = listOf("**/$projectInfoClassName**")
            limit {
                counter = "INSTRUCTION"
                value = "COVEREDRATIO"
                minimum = "0.97".toBigDecimal()
            }
            limit {
                counter = "BRANCH"
                value = "COVEREDRATIO"
                minimum = "0.95".toBigDecimal()
            }
        }
    }
}

tasks.named("check") {
    dependsOn(tasks.withType<JacocoReport>())
    dependsOn(tasks.withType<JacocoCoverageVerification>())
}
