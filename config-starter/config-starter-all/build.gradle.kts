dependencies {
    project.parent?.subprojects?.filter {
        !it.name.endsWith("-testing") && !it.name.equals(project.name)
    }?.forEach { api(it) }
}
