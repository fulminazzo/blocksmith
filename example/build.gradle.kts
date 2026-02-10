plugins {
    alias(libs.plugins.shadow)
}

dependencies {
    implementation(project(":config-starter:config-starter-all"))
    implementation(project(":data-starter:data-starter-file"))
}

tasks.jar {
    dependsOn(tasks.shadowJar)
}

tasks.shadowJar {
    archiveClassifier = "all"
}
