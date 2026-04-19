dependencies {
    api(projects.base.validation)

    project.parent?.let { api(it) }
}
