dependencies {
    compileOnly(libs.slf4j)

    api(libs.json)

    api(libs.validation)

    testImplementation(libs.slf4j)
    testImplementation(libs.bundles.log4j)
}
