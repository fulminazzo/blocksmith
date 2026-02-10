plugins {
    alias(libs.plugins.shadow)
}

dependencies {
    implementation(libs.slf4j)
    implementation(libs.bundles.log4j)

    implementation(project(":config-starter:config-starter-all"))
    implementation(project(":data-starter:data-starter-file"))
}

tasks.jar {
    dependsOn(tasks.shadowJar)
}

tasks.shadowJar {
    archiveClassifier = "all"
}
