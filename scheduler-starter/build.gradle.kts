val projectName: String = project.name

subprojects {
    dependencies {
        api(project(":$projectName"))
    }
}
