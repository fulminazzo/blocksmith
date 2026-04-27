/**
 * Convention plugin identifying a composite module.
 *
 * A composite module requires a "<module>-<baseName>" subproject to be passed.
 * Each subproject in the module will depend on the base subproject.
 * The parent project will depend on all subprojects (except those in the excludedSubmodules set).
 */

plugins {
    id("java-library")
}

interface CompositeModuleExtension {
    val excludedSubmodules: Set<String>
}

val extension = extensions.create<CompositeModuleExtension>("compositeModule")


afterEvaluate {
    val baseModuleName: String by extra
    val testingModuleName: String by extra

    val excludedSubmodules = extension.excludedSubmodules

    dependencies {
        val path = project.path
        val baseProject = project("$path$path-$baseModuleName")

        subprojects {

            dependencies {
                if (project.path != baseProject.path)
                    api(baseProject)
            }

        }

        subprojects
            .filter { !it.name.endsWith(testingModuleName) }
            .filter { it.name !in excludedSubmodules }
            .forEach { api(it) }
    }

}
