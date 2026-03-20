val projectName: String = project.name

subprojects {
    dependencies {
        api(project(":$projectName"))

        val baseProjectName = "$projectName-base"
        if (project.name != baseProjectName)
            api(project(":$projectName:$baseProjectName"))
    }
}
