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
    val baseProjectName = "base"
    val excludedSubmodules = extension.excludedSubmodules

    dependencies {
        val path = project.path

        val baseProject = project("$path$path-$baseProjectName")
        subprojects {

            dependencies {
                if (project.path != baseProject.path && project.name !in excludedSubmodules)
                    api(baseProject)
            }

        }

        subprojects.forEach { api(it) }
    }

}
