val testingModuleName: String by rootProject.extra

dependencies {
    project.parent?.subprojects?.filter {
        !it.name.endsWith("-${testingModuleName}") && !it.name.equals(project.name)
    }?.forEach { api(it) }
}
