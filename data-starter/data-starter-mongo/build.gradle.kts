dependencies {
    api(libs.validation)

    implementation(platform(libs.mongodb.driver.platform))

    api(libs.mongodb.driver.async)
    api(libs.reactor)

    testImplementation(libs.slf4j)
    testImplementation(libs.bundles.log4j)
    testImplementation(libs.embedded.mongodb)
}
