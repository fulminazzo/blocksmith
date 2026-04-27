dependencies {
    implementation(platform(libs.mongodb.driver.platform))

    api(libs.mongodb.driver.async)
    api(libs.reactor)

    testImplementation(libs.embedded.mongodb)
}
