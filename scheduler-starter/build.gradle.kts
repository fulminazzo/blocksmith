subprojects {
    dependencies {
        project.parent?.let { api(it) }
    }
}
