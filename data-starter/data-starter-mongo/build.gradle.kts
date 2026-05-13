@file:Suppress("UnstableApiUsage")

dependencies {
    implementation(platform(libs.mongodb.driver.platform))

    api(libs.mongodb.driver.async)
    api(libs.reactor)
}

testing {
    suites {
        withType<JvmTestSuite> {
            dependencies {
                implementation(libs.test.containers.mongodb)
            }
        }
    }
}
