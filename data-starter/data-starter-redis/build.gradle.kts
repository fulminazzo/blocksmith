dependencies {
    api(libs.lettuce)

    api(project(":data-starter:data-starter-mapper:data-starter-mapper-json"))

    testImplementation(libs.bundles.log4j)
}
