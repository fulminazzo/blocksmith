dependencies {
    api(projects.validationStarter)

    project.parent?.let { api(it) }
}
