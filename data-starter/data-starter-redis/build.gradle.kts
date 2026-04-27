dependencies {
    api(libs.lettuce)

    api(projects.dataStarter.dataStarterMapper.dataStarterMapperJson)

    testImplementation(libs.embedded.redis)
}
