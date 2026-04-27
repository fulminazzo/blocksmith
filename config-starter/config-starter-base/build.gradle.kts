dependencies {
    api(libs.jackson.json)

    api(projects.validationStarter)

    project.parent?.let { api(it) }
}
