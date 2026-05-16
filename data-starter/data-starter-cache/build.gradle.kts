@file:Suppress("UnstableApiUsage")

dependencies {
    api(projects.dataStarter.dataStarterMemory)
}

testing {
    suites {
        withType<JvmTestSuite> {
            dependencies {
                // CACHE
                implementation(projects.dataStarter.dataStarterRedis)

                // BASE
                implementation(projects.dataStarter.dataStarterSql)
                implementation(libs.postgresql)
                implementation(libs.test.containers.postgresql)
            }
        }
    }
}
