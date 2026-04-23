dependencies {
    api(projects.validationStarter)

    api(libs.lettuce)

    api(projects.dataStarter.dataStarterMapper.dataStarterMapperJson)

    testImplementation(libs.bundles.log4j)
    testImplementation(libs.embedded.redis)
}
