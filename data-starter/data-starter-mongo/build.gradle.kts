@file:Suppress("UnstableApiUsage")

dependencies {
    implementation(platform(libs.mongodb.driver.platform))

    api(libs.mongodb.driver.async)
    api(libs.reactor)

    integrationTestImplementation(libs.test.containers.mongodb)
}
