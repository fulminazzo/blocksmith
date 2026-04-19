subprojects {
    dependencies {
        val baseProject = rootProject.projects.commandStarter.commandStarterBase
        if (project.path != baseProject.path) api(baseProject)

        project.parent?.let { api(it) }
    }
}
