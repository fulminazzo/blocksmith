/**
 * Plugin for applying the Blocksmith BuildConfig configuration across submodules.
 */

plugins {
    id("com.github.gmazzo.buildconfig")
}

val projectInfoClassName: String by extra

val baseModuleName: String by extra

configure<com.github.gmazzo.buildconfig.BuildConfigExtension> {
    packageName = "${rootProject.group}.${rootProject.name}"
    className = projectInfoClassName

    var projectName = project.name
    if (project.name.endsWith("-$baseModuleName")) projectName = project.name.removeSuffix("-$baseModuleName")

    buildConfigField("String", "GROUP", "\"${rootProject.group}\"")
    buildConfigField("String", "PROJECT_NAME", "\"${rootProject.name}\"")
    buildConfigField("String", "MODULE_NAME", "\"${projectName}\"")
}
