plugins { id("blocksmith.composite-module") }

dependencies {
    api(project.projects.messageBrokerStarter.pluginMessaging.pluginMessagingBase)
}

compositeModule {
    excludedSubmodules = setOf(
        project.projects.messageBrokerStarter.pluginMessaging.name
    )
}