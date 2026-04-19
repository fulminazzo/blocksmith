dependencies {
    api(libs.jackson.json)

    api(projects.base.validation)

    project.parent?.let { api(it) }
}
