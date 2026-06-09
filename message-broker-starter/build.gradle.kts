plugins { id("blocksmith.composite-module") }

compositeModule {
    excludedSubmodules = setOf(
        rootProject.projects.messageBrokerStarter.pluginMessaging.name
    )
}
