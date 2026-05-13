plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
    implementation(pluginToDependency(libs.plugins.buildconfig))
    implementation(pluginToDependency(libs.plugins.sonarqube))
    implementation(pluginToDependency(libs.plugins.spotbugs))

    subprojects.forEach { implementation(it) }
}

private fun pluginToDependency(plugin: Provider<PluginDependency>) =
    plugin.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" }