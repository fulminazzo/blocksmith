dependencies {
    api(projects.dataStarter.dataStarterBase)

    integrationTestImplementation(projects.configStarter.configStarterYaml)
    integrationTestImplementation(projects.dataStarter.dataStarterCache)
    integrationTestImplementation(projects.dataStarter.dataStarterRedis)
    integrationTestImplementation(projects.dataStarter.dataStarterSql)
}
