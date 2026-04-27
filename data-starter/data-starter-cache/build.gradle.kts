dependencies {
    api(projects.dataStarter.dataStarterMemory)

    testImplementation(projects.dataStarter.dataStarterRedis)
    testImplementation(projects.dataStarter.dataStarterSql)

    testImplementation(libs.embedded.redis)
    testImplementation(libs.h2)
}