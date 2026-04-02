val testingModuleName: String by rootProject.extra

dependencies {
    project.parent?.subprojects?.filter {
        !it.name.endsWith("-${testingModuleName}") && it.name != project.name
    }?.forEach { api(it) }
}
