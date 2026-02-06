dependencies {
    compileOnly(libs.slf4j)

    api(libs.jackson.json)
    api(libs.jackson.yaml)
    api(libs.bundles.toml)
    api(libs.jackson.xml)
    api(libs.jackson.properties)

    api(libs.bundles.validation)

    testImplementation(libs.slf4j)
    testImplementation(libs.bundles.log4j)
}
