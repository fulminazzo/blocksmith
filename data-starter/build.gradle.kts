dependencies {
    val projectName = project.name

    subprojects {
        dependencies {
            api(project(":$projectName"))
        }
    }

}
