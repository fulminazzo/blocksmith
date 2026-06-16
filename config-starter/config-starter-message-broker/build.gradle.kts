dependencies {
    api(projects.messageBrokerStarter.messageBrokerStarterBase)

    integrationTestImplementation(projects.configStarter.configStarterYaml)
    integrationTestImplementation(projects.messageBrokerStarter.messageBrokerStarterRedis)
}
