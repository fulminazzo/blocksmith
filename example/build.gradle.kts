plugins {
    alias(libs.plugins.shadow)
}

dependencies {
    implementation(projects.configStarter.configStarterAll)
}

tasks.jar {
    dependsOn(tasks.shadowJar)
}

tasks.shadowJar {
    archiveClassifier = "all"
}
