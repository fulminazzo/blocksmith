plugins {
    alias(libs.plugins.shadow)
}

dependencies {
    api(libs.embedded.redis)

    api(project(":data-starter:data-starter-redis"))
}

tasks.jar {
    dependsOn(tasks.shadowJar)
}

tasks.shadowJar {
    archiveClassifier = "all"
}
