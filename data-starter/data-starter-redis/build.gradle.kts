dependencies {
    api(projects.base.validation)

    api(libs.lettuce)

    api(projects.dataStarter.dataStarterMapper.dataStarterMapperJson)

    testImplementation(libs.bundles.log4j)
    testImplementation(libs.embedded.redis)
}
