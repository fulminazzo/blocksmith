/**
 * Convention plugin identifying a composite module.
 *
 * A composite module requires a "<module>-<baseModuleName>" subproject to be passed.
 * Each subproject in the module will depend on the base subproject.
 * The parent project will depend on all subprojects (except those in the excludedSubmodules set).
 */

plugins {
    `java-library`
}

interface CompositeModuleExtension {
    /**
     * These submodules will not be imported to the main module.
     * For example, if the module is "blocksmith-core",
     * the submodules "blocksmith-core-bukkit" and "blocksmith-core-bungeecord" will not be imported.
     */
    val excludedSubmodules: SetProperty<String>

    /**
     * Modules for which the base module import will not happen.
     */
    val ignoredSubmodules: SetProperty<String>
    val importToParent: Property<Boolean>
}

val extension = extensions.create<CompositeModuleExtension>("compositeModule")


afterEvaluate {
    val baseModuleName: String by extra
    val testingModuleName: String by extra

    val excludedSubmodules = extension.excludedSubmodules
    val ignoredSubmodules = extension.ignoredSubmodules

    dependencies {
        val baseProject = project("${project.path}:${project.name}-$baseModuleName")

        subprojects {

            dependencies {
                if (project.path != baseProject.path &&
                    ignoredSubmodules.getOrElse(emptySet()).none { it in project.name }
                ) api(baseProject)
            }

        }

        if (extension.importToParent.getOrElse(true))
            subprojects
                .filter { !it.name.endsWith(testingModuleName) }
                .filter { p ->
                    excludedSubmodules.getOrElse(emptySet()).none { it in p.name }
                }
                .forEach { api(it) }
    }

}
