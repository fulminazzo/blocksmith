dependencies {
    compileOnly(libs.slf4j)

    api(libs.json)
    api(libs.yaml)
    api(libs.toml)

    api(libs.bundles.validation)

    testImplementation(libs.slf4j)
    testImplementation(libs.bundles.log4j)
}
