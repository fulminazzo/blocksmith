val projectName: String = project.name

allprojects {
    dependencies {
        api(rootProject.libs.minimessage)
    }
}

subprojects {
    dependencies {
        api(project(":$projectName"))
    }
}

dependencies {
    api(libs.slf4j)
    api(project(":config-starter"))

    testImplementation(project(":config-starter:config-starter-yaml"))
}
