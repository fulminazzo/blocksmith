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
