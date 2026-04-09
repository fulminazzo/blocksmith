val testingModuleName: String by rootProject.extra

dependencies {
    project.parent?.subprojects?.filter {
        !it.name.endsWith("-${testingModuleName}") &&
                !it.name.equals(project.name) &&
                !it.name.contains("generator")
    }?.forEach { api(it) }
}
