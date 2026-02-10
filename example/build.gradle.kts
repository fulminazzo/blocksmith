plugins {
    alias(libs.plugins.shadow)
}

dependencies {
    implementation(project(":config-starter:config-starter-all"))
}

tasks.jar {
    dependsOn(tasks.shadowJar)
}

tasks.shadowJar {
    archiveClassifier = "all"
}
