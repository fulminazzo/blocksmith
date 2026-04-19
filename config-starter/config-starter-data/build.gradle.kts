dependencies {
    api(projects.dataStarter.dataStarterBase)

    testImplementation(projects.configStarter.configStarterYaml)
    testImplementation(projects.dataStarter.dataStarterCache)
    testImplementation(projects.dataStarter.dataStarterRedis)
    testImplementation(projects.dataStarter.dataStarterSql)
}
