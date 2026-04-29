plugins {
    alias(libs.plugins.shadow)
}

dependencies {
    implementation(projects.configStarter)
}

tasks.jar {
    dependsOn(tasks.shadowJar)
}

tasks.shadowJar {
    archiveClassifier = "all"
}
